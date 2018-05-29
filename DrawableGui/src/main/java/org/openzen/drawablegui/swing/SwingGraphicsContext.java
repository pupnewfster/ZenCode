/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.GeneralPath;
import java.util.WeakHashMap;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DDrawingContext;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DPathTracer;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingGraphicsContext implements DDrawingContext {
	public final float scale;
	private final WeakHashMap<DPath, GeneralPath> preparedPaths = new WeakHashMap<>();
	private final SwingRoot root;
	private Graphics graphics;
	
	public SwingGraphicsContext(float scale, SwingRoot root) {
		this.scale = scale;
		this.root = root;
	}
	
	public GeneralPath getPath(DPath path) {
		GeneralPath generalPath = preparedPaths.get(path);
		if (generalPath == null) {
			generalPath = new GeneralPath();
			path.trace(new PathTracer(generalPath));
			preparedPaths.put(path, generalPath);
		}
		return generalPath;
	}
	
	@Override
	public float getScale() {
		return scale;
	}
	
	@Override
	public void repaint(int x, int y, int width, int height) {
		root.repaint(x, y, width, height);
	}

	@Override
	public void setCursor(Cursor cursor) {
		int translated = java.awt.Cursor.DEFAULT_CURSOR;
		switch (cursor) {
			case NORMAL:
				translated = java.awt.Cursor.DEFAULT_CURSOR;
				break;
			case MOVE:
				translated = java.awt.Cursor.MOVE_CURSOR;
				break;
			case HAND:
				translated = java.awt.Cursor.HAND_CURSOR;
				break;
			case TEXT:
				translated = java.awt.Cursor.TEXT_CURSOR;
				break;
			case E_RESIZE:
				translated = java.awt.Cursor.E_RESIZE_CURSOR;
				break;
			case S_RESIZE:
				translated = java.awt.Cursor.S_RESIZE_CURSOR;
				break;
			case NE_RESIZE:
				translated = java.awt.Cursor.NE_RESIZE_CURSOR;
				break;
			case NW_RESIZE:
				translated = java.awt.Cursor.NW_RESIZE_CURSOR;
				break;
		}
		root.setCursor(java.awt.Cursor.getPredefinedCursor(translated));
	}

	@Override
	public DFontMetrics getFontMetrics(DFont font) {
		if (graphics == null)
			graphics = root.getGraphics();
		
		SwingCanvas.prepare(font);
		return new SwingFontMetrics(graphics.getFontMetrics((Font) font.cached), graphics);
	}
	
	@Override
	public void focus(DComponent component) {
		this.root.focus(component);
	}

	@Override
	public void scrollInView(int x, int y, int width, int height) {
		// not in a scrollable context
	}
	
	private class PathTracer implements DPathTracer {
		private final GeneralPath path;
		
		public PathTracer(GeneralPath path) {
			this.path = path;
		}

		@Override
		public void moveTo(float x, float y) {
			path.moveTo(x, y);
		}

		@Override
		public void lineTo(float x, float y) {
			path.lineTo(x, y);
		}

		@Override
		public void bezierCubic(float x1, float y1, float x2, float y2, float x3, float y3) {
			path.curveTo(x1, y1, x2, y2, x3, y3);
		}

		@Override
		public void bezierQuadratic(float x1, float y1, float x2, float y2) {
			path.quadTo(x1, y1, x2, y2);
		}

		@Override
		public void close() {
			path.closePath();
		}
	}
}
