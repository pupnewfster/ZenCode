/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.style.DDpDimension;
import org.openzen.drawablegui.style.DPxDimension;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewTabStyle {
	public final DFont tabFont;
	public final int tabFontColor;
	
	public final int tabColorNormal;
	public final int tabColorHover;
	public final int tabColorPress;
	public final int tabColorActive;
	
	public final int paddingTop;
	public final int paddingBottom;
	public final int paddingLeft;
	public final int paddingRight;
	
	public final int borderColor;
	public final int borderWidth;
	
	public final int closeIconSize;
	public final int closeIconPadding;
	
	public TabbedViewTabStyle(DStyleDefinition style) {
		tabFont = style.getFont("tabFont", context -> new DFont(DFontFamily.UI, false, false, false, (int)(12 * context.getScale())));
		tabFontColor = style.getColor("tabFontColor", 0xFF000000);
		
		tabColorNormal = style.getColor("tabColorNormal", 0xFFEEEEEE);
		tabColorHover = style.getColor("tabColorHover", 0xFFFFFFFF);
		tabColorPress = style.getColor("tabColorPress", 0xFFF0F0F0);
		tabColorActive = style.getColor("tabColorActive", 0xFFFFFFFF);
		
		paddingTop = style.getDimension("paddingTop", new DDpDimension(4));
		paddingBottom = style.getDimension("paddingTop", new DDpDimension(4));
		paddingLeft = style.getDimension("paddingLeft", new DDpDimension(4));
		paddingRight = style.getDimension("paddingRight", new DDpDimension(4));
		
		borderColor = style.getColor("borderColor", 0xFF888888);
		borderWidth = style.getDimension("borderWidth", new DPxDimension(1));
		
		closeIconSize = style.getDimension("closeIconSize", new DDpDimension(16));
		closeIconPadding = style.getDimension("closeIconPadding", new DDpDimension(6));
	}
}