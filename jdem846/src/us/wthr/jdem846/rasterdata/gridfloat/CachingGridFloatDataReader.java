package us.wthr.jdem846.rasterdata.gridfloat;

import java.io.File;

import us.wthr.jdem846.ByteOrder;
import us.wthr.jdem846.DemConstants;
import us.wthr.jdem846.exception.DataSourceException;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;

public class CachingGridFloatDataReader
{
	private static Log log = Logging.getLog(CachingGridFloatDataReader.class);
	
	private GridFloatDataReader dataReader = null;
	private boolean isDisposed = false;
	
	private int bufferX = -1;
	private int bufferY = -1;
	private int bufferRows = -1;
	private int bufferColumns = -1;
	private double[][] buffer = null;
	
	public CachingGridFloatDataReader(File dataFile, int rows, int columns, ByteOrder byteOrder)
	{
		dataReader = new GridFloatDataReader(dataFile, rows, columns, byteOrder);
	}
	
	public void dispose() throws DataSourceException
	{
		if (isDisposed()) {
			throw new DataSourceException("Raster data reader already disposed.");
		}
		
		dataReader.dispose();
		
		// TODO: Finish
		isDisposed = true;
	}

	
	public boolean isDisposed()
	{
		return isDisposed;
	}
	
	public boolean isBufferFilled()
	{
		return (buffer != null);
	}
	
	public boolean fillBuffer(int x, int y, int columns, int rows) throws DataSourceException
	{
		if (isBufferFilled()) {
			throw new DataSourceException("Buffer is already filled");
		}
		
		bufferX = x;
		bufferY = y;
		bufferRows = rows;
		bufferColumns = columns;
		
		buffer = new double[rows][columns];
		
		dataReader.get(x, y, buffer);

		return false;
	}
	
	public void clearBuffer() throws DataSourceException
	{
		bufferX = -1;
		bufferY = -1;
		bufferRows = -1;
		bufferColumns = -1;
		buffer = null;
	}
	
	public boolean isPointInBuffer(int row, int column)
	{

		if (isBufferFilled() &&
				row >= bufferY 
				&& row < bufferY + bufferColumns
				&& column >= bufferX
				&& column < bufferX + bufferRows) {
			return true;
		} else {
			return false;
		}
	}
	
	public double get(int row, int column) throws DataSourceException
	{
		if (dataReader.isDisposed()) {
			throw new DataSourceException("Data reader has been disposed.");
		}
		
		if (isPointInBuffer(row, column)) {
			int bufferRow = row - bufferY;
			int bufferColumn = column - bufferX;
			
			if (bufferRow < 0 || bufferRow >= bufferRows) {
				throw new DataSourceException("Invalid buffer row " + bufferRow);
			}
			
			if (bufferColumn < 0 || bufferColumn >= bufferColumns) {
				throw new DataSourceException("Invalid buffer column " + bufferColumn);
			}
			
			return buffer[bufferRow][bufferColumn];
		} else {
			return dataReader.get(row, column);
		}
	}
	
	
	public void open() throws DataSourceException
	{
		if (dataReader.isDisposed()) {
			throw new DataSourceException("Data reader has been disposed.");
		}
		dataReader.open();
	}
	
	
	public void close() throws DataSourceException
	{
		if (dataReader.isDisposed()) {
			throw new DataSourceException("Data reader has been disposed.");
		}
		dataReader.close();
	}
}
