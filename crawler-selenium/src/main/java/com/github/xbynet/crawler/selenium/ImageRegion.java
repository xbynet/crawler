package com.github.xbynet.crawler.selenium;

/**
 * @author taojw
 *
 */
public class ImageRegion {
	public int x;
	public int y;
	public int width;
	public int height;
	public ImageRegion(int x,int y,int width,int height){
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	}
	@Override
	public String toString() {
		return "ImageRegion [x=" + x + ", y=" + y + ", width=" + width
				+ ", height=" + height + "]";
	}
	
}
