/*
 * Copyright (C) 2011 Kevin M. Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.wthr.jdem846.input.esri;

import java.io.File;

import us.wthr.jdem846.DemConstants;
import us.wthr.jdem846.annotations.ElevationDataLoader;
import us.wthr.jdem846.exception.DataSourceException;
import us.wthr.jdem846.input.DataSource;
import us.wthr.jdem846.input.bil.BilInt16;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;

@ElevationDataLoader(name="us.wthr.jdem846.input.esri.gridAscii.name", identifier="esri-gridascii", extension="asc")
public class GridAscii extends DataSource
{
	private static Log log = Logging.getLog(GridAscii.class);
	
	private GridAsciiHeader header;
	
	private int bits = 16;
	private int totalRowBytes;
	private double maxDifference;
	private double maxElevation;
    private double minElevation;
    private double resolution;
    private int maxRow;
    private int maxCol;
    private String filePath;
    
    private int cachedRow = -1;
    private GridAsciiDataCache cache;
    
    private boolean isDisposed = false;
    
    public GridAscii(String filePath)
    {
    	this.filePath = filePath;
    	File ascFile = new File(filePath);
		
		header = new GridAsciiHeader(ascFile);
		cache = new GridAsciiDataCache(ascFile, header.getColumns());
		
		this.calculateDistances();
    }

    public long getDataLength()
	{
		return cache.getDataLength();
	}
    
	public void dispose() throws DataSourceException
	{
		if (isDisposed) {
			throw new DataSourceException("Object already disposed of");
		}
		
		cache.dispose();
		cache = null;
		
		isDisposed = true;
	}
    
	private double __get(int row, int col)  throws DataSourceException
	{
		if (col < 0 || col > header.getColumns() || row < 0 || row > header.getRows())
			return DemConstants.ELEV_NO_DATA;
		
		if (row != cachedRow && row != cachedRow+1) {
			this.loadRow(row);
		}
		
		if (row != cachedRow) {
			col = header.getColumns() + col;
		}
	
		if (col >= header.getColumns() * 2) {
			log.warn("Column too high!!! -- " + row + "/" + col + " -- " + header.getColumns() + " -- " + col);
		}
		
		double elevation = cache.get(col);
		return elevation;
	}
	
	
	public double getElevation(int col) throws DataSourceException
	{
		if (col >= header.getColumns())
			return DemConstants.ELEV_NO_DATA;
		
		return this.getElevation(cachedRow, col);
	}

	public double getElevation(int row, int column) throws DataSourceException
	{
		if (column >= header.getColumns() || row > header.getRows())
			return DemConstants.ELEV_NO_DATA;
		
		double elevation = this.__get(row, column);
		if (elevation == header.getNoData())
			elevation = DemConstants.ELEV_NO_DATA;

		return elevation;
	}
    

	@Override
	public void initDataCache() throws DataSourceException
	{
		loadRow(this.cachedRow);
	}
	
	@Override
	public void load(double[] valueBuffer, int start, int length) throws DataSourceException
	{
		cache.load(valueBuffer, start, length);
	}

	@Override
	public void loadRow(int row) throws DataSourceException
	{
		this.cachedRow = row;
		long seekTo = (long) row * (long)(header.getColumns() * 4l);
		cache.load(seekTo);
	}

	@Override
	public void unloadDataCache()
	{
		cache.unload();
	}
    
    
    
	public GridAsciiHeader getHeader()
	{
		return header;
	}

	public void setHeader(GridAsciiHeader header)
	{
		this.header = header;
	}

	public int getBits()
	{
		return bits;
	}

	public void setBits(int bits)
	{
		this.bits = bits;
	}

	public int getTotalRowBytes()
	{
		return totalRowBytes;
	}

	public void setTotalRowBytes(int totalRowBytes)
	{
		this.totalRowBytes = totalRowBytes;
	}

	public double getMaxDifference()
	{
		return maxDifference;
	}

	public void setMaxDifference(double maxDifference)
	{
		this.maxDifference = maxDifference;
	}

	public double getMaxElevation()
	{
		return maxElevation;
	}

	public void setMaxElevation(double maxElevation)
	{
		this.maxElevation = maxElevation;
	}

	public double getMinElevation()
	{
		return minElevation;
	}

	public void setMinElevation(double minElevation)
	{
		this.minElevation = minElevation;
	}

	public double getResolution()
	{
		return resolution;
	}

	public void setResolution(double resolution)
	{
		this.resolution = resolution;
	}

	public int getMaxRow()
	{
		return maxRow;
	}

	public void setMaxRow(int maxRow)
	{
		this.maxRow = maxRow;
	}

	public int getMaxCol()
	{
		return maxCol;
	}

	public void setMaxCol(int maxCol)
	{
		this.maxCol = maxCol;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public int getCachedRow()
	{
		return cachedRow;
	}

	public void setCachedRow(int cachedRow)
	{
		this.cachedRow = cachedRow;
	}

	public GridAsciiDataCache getCache()
	{
		return cache;
	}

	public void setCache(GridAsciiDataCache cache)
	{
		this.cache = cache;
	}


	public DataSource copy()
	{
		GridAscii dataSource = new GridAscii(this.filePath);
		dataSource.bits = this.bits;
		dataSource.totalRowBytes = this.totalRowBytes;
		dataSource.maxDifference = this.maxDifference;
		dataSource.maxElevation = this.maxElevation;
		dataSource.minElevation = this.minElevation;
		dataSource.resolution = this.resolution;
		dataSource.maxRow = this.maxRow;
		dataSource.maxCol = this.maxCol;
		dataSource.filePath = this.filePath;
	    
		dataSource.cachedRow = -1;
		
	    return dataSource;
	}



    
    
    
}