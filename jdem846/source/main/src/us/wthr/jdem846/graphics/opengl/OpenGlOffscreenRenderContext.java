package us.wthr.jdem846.graphics.opengl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2.GLUgl2;

public class OpenGlOffscreenRenderContext
{
	
	protected GLProfile glProfile;
	protected GLCapabilities glCapabilities;
	protected GLContext glContext;
	protected GLU glu = new GLU();
	protected GLUgl2 glugl2 = new GLUgl2();
	protected GLAutoDrawable drawable;
	
	
	public OpenGlOffscreenRenderContext(int width, int height)
	{
		glProfile = GLProfile.getDefault();
		GLDrawableFactory fac = GLDrawableFactory.getFactory(glProfile);
		glCapabilities = new GLCapabilities(glProfile);
		glCapabilities.setDoubleBuffered(false);
		glCapabilities.setOnscreen(false);
		glCapabilities.setPBuffer(true);
		glCapabilities.setNumSamples(1);
		
		drawable = fac.createOffscreenAutoDrawable(null, glCapabilities, null, width, height, null);
		glContext = drawable.getContext();
		
	}
	
	public GLProfile getGlProfile()
	{
		return glProfile;
	}
	
	public void dispose()
	{
		drawable.destroy();
		glContext.destroy();
	}
	
	public void makeGlContextCurrent()
	{
		this.glContext.makeCurrent();
	}
	
	public GL getGL()
	{
		return glContext.getGL();
	}
	
	public GL2 getGL2()
	{
		return getGL().getGL2();
	}
	
	public GLContext getGLContext()
	{
		return glContext;
	}
	
	public GLAutoDrawable getDrawable()
	{
		return drawable;
	}
	
	public GLU getGLU()
	{
		return glu;
	}
	
	public GLUgl2 getGLUgl2()
	{
		return glugl2;
	}
}
