/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DSimpleTooltipComponent implements DComponent {
	private final DStyleClass styleClass;
	private final LiveString tooltip;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final ListenerHandle<LiveString.Listener> tooltipListener;
	
	private DDrawSurface surface;
	private int z;
	private DIRectangle bounds;
	private DFontMetrics fontMetrics;
	private DSimpleTooltipStyle style;
	
	private DDrawnRectangle background;
	private DDrawnText text;
	
	public DSimpleTooltipComponent(DStyleClass styleClass, LiveString tooltip) {
		this.styleClass = styleClass;
		this.tooltip = tooltip;
		tooltipListener = tooltip.addListener(this::onTooltipChanged);
	}
	
	private void onTooltipChanged(String oldValue, String newValue) {
		if (surface == null || bounds == null)
			return;
		
		calculateSize();
		bounds = new DIRectangle(
				bounds.x,
				bounds.y,
				style.border.getPaddingLeft() + fontMetrics.getWidth(tooltip.getValue()) + style.border.getPaddingRight(),
				style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingBottom());
		surface.repaint(bounds);
		
		if (text != null)
			text.close();
		
		text = surface.drawText(
				z,
				style.font,
				style.textColor,
				bounds.x + style.border.getPaddingLeft(),
				bounds.y + style.border.getPaddingTop() + fontMetrics.getAscent(),
				newValue);
	}
	
	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		style.border.update(surface, z + 1, bounds);
		
		if (background != null)
			background.close();
		background = surface.fillRect(z, bounds, style.backgroundColor);
		text.setPosition(
				bounds.x + style.border.getPaddingLeft(),
				bounds.y + style.border.getPaddingTop() + fontMetrics.getAscent());
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		return style.border.getPaddingTop() + fontMetrics.getAscent();
	}
	
	@Override
	public LiveObject<DSizing> getSizing() {
		return sizing;
	}
	
	@Override
	public void setSurface(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		
		DStylePath path = parent.getChild("tooltip", styleClass);
		style = new DSimpleTooltipStyle(surface.getStylesheet(path));
		fontMetrics = surface.getFontMetrics(style.font);
		calculateSize();
		
		text = surface.drawText(z + 1, style.font, style.textColor, 0, 0, tooltip.getValue());
	}
	
	@Override
	public void paint(DCanvas canvas) {
		
	}
	
	@Override
	public void close() {
		tooltipListener.close();
	}
	
	private void calculateSize() {
		sizing.setValue(new DSizing(
				style.border.getPaddingLeft() + fontMetrics.getWidth(tooltip.getValue()) + style.border.getPaddingRight(),
				style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingBottom()));
	}
}
