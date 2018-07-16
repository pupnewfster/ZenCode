/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.tree;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DTreeView<N extends DTreeNode<N>> implements DComponent {
	private final DStyleClass styleClass;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private DIRectangle bounds;
	
	private int selectedRow = -1;
	private N selectedNode = null;
	
	private DTreeViewStyle style;
	private DFontMetrics fontMetrics;
	
	private final DDrawable nodeOpenedIcon;
	private final DDrawable nodeClosedIcon;
	
	private DDrawSurface surface;
	private final N root;
	private final boolean showRoot;
	private final List<Row> rows = new ArrayList<>();
	
	public DTreeView(
			DStyleClass styleClass,
			DDrawable nodeOpenedIcon,
			DDrawable nodeClosedIcon,
			N root,
			boolean showRoot)
	{
		this.styleClass = styleClass;
		this.root = root;
		this.showRoot = showRoot;
		
		this.nodeOpenedIcon = nodeOpenedIcon;
		this.nodeClosedIcon = nodeClosedIcon;
	}

	@Override
	public void setSurface(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		style = new DTreeViewStyle(surface.getStylesheet(parent.getChild("tree", styleClass)));
		fontMetrics = surface.getFontMetrics(style.font);
		
		updateLayout();
	}

	@Override
	public MutableLiveObject<DSizing> getSizing() {
		return sizing;
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		return -1;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		
		int drawX = bounds.x + style.padding;
		int drawY = bounds.y + style.padding;
		if (showRoot) {
			paintNode(canvas, root, drawX, drawY);
		} else {
			for (N child : root.getChildren()) {
				drawY = paintNode(canvas, child, drawX, drawY);
				drawY += style.rowSpacing;
			}
		}
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		int row = yToRow(e.y);
		if (row >= 0 && row < rows.size()) {
			Row rowEntry = rows.get(row);
			if (e.x >= rowEntry.x && e.x < (rowEntry.x + nodeOpenedIcon.getNominalWidth())) {
				if (!rowEntry.node.isLeaf())
					rowEntry.node.isCollapsed().toggle();
				return;
			}
			
			rowEntry.node.onMouseClick(e);
			
			if (e.isSingleClick()) {
				int oldRow = selectedRow;
				selectedRow = row;
				selectedNode = rows.get(row).node;

				if (oldRow >= 0)
					surface.repaint(
							bounds.x,
							rowToY(oldRow) - style.selectedPaddingTop,
							bounds.width,
							fontMetrics.getAscent() + fontMetrics.getDescent() + style.selectedPaddingTop + style.selectedPaddingBottom);
				surface.repaint(
						bounds.x,
						rowToY(row) - style.selectedPaddingTop,
						bounds.width,
						fontMetrics.getAscent() + fontMetrics.getDescent() + style.selectedPaddingTop + style.selectedPaddingBottom);
			}
		}
	}
	
	private int rowToY(int row) {
		return row * (style.rowSpacing + fontMetrics.getAscent() + fontMetrics.getDescent()) + style.padding;
	}
	
	private int yToRow(int y) {
		return (y - bounds.y - style.padding) / (style.rowSpacing + fontMetrics.getAscent() + fontMetrics.getDescent());
	}
	
	private int paintNode(DCanvas canvas, N node, int drawX, int drawY) {
		int textColor = style.nodeTextColor;
		if (node == selectedNode) {
			textColor = style.selectedNodeTextColor;
			canvas.fillRectangle(
					bounds.x,
					drawY - style.selectedPaddingTop,
					bounds.width,
					fontMetrics.getAscent() + fontMetrics.getDescent() + style.selectedPaddingTop + style.selectedPaddingBottom,
					style.selectedBackgroundColor);
		}
		
		int drawingX = drawX;
		if (!node.isLeaf()) {
			DDrawable icon = node.isCollapsed().getValue() ? nodeClosedIcon : nodeOpenedIcon;
			icon.draw(canvas, DTransform2D.translate(drawingX, drawY + fontMetrics.getAscent() + fontMetrics.getDescent() - icon.getNominalHeight()));
			drawingX += style.iconTextSpacing + icon.getNominalWidth();
		} else {
			drawingX += style.iconTextSpacing + nodeClosedIcon.getNominalWidth();
		}
		node.getIcon().draw(canvas, DTransform2D.translate(drawingX, drawY + fontMetrics.getAscent() + fontMetrics.getDescent() - node.getIcon().getNominalHeight()), textColor);
		drawingX += style.iconTextSpacing + node.getIcon().getNominalWidth();
		canvas.drawText(style.font, textColor, drawingX, drawY + fontMetrics.getAscent(), node.getTitle());
		drawY += fontMetrics.getAscent() + fontMetrics.getDescent();
		
		if (!node.isCollapsed().getValue()) {
			for (N child : node.getChildren()) {
				drawY += style.rowSpacing;
				drawY = paintNode(canvas, child, drawX + style.indent, drawY);
			}
		}
		
		return drawY;
	}
	
	private void updateLayout() {
		int oldRowCount = rows.size();
		
		for (Row row : rows)
			row.close();
		
		rows.clear();
		
		if (showRoot) {
			updateLayout(root, 0);
		} else {
			for (N child : root.getChildren()) {
				updateLayout(child, 0);
			}
		}
		
		if (rows.size() != oldRowCount) {
			DSizing preferences = sizing.getValue();
			sizing.setValue(new DSizing(
							preferences.minimumWidth,
							preferences.minimumHeight,
							preferences.preferredWidth,
							rowToY(rows.size()),
							preferences.maximumWidth,
							1000000));
		}
		
		System.out.println("Rows after updateLayout: " + rows.size());
		System.out.println("Height: " + rowToY(rows.size()));
	}
	
	private void updateLayout(N node, int x) {
		rows.add(new Row(x, node));
		
		if (!node.isCollapsed().getValue()) {
			for (N child : node.getChildren()) {
				updateLayout(child, x + style.indent);
			}
		}
	}

	@Override
	public void close() {
		// nothing to clean up
	}
	
	private class Row implements Closeable, LiveBool.Listener, LiveList.Listener<N> {
		private final int x;
		private final N node;
		private final ListenerHandle<LiveBool.Listener> collapseListener;
		private final ListenerHandle<LiveList.Listener<N>> childListener;
		
		public Row(int x, N node) {
			this.x = x;
			this.node = node;
			this.collapseListener = node.isCollapsed().addListener(this);
			this.childListener = node.getChildren().addListener(this);
		}

		@Override
		public void onChanged(boolean oldValue, boolean newValue) {
			updateLayout();
			surface.repaint(bounds);
		}

		@Override
		public void onInserted(int index, N value) {
			updateLayout();
			surface.repaint(bounds);
		}

		@Override
		public void onChanged(int index, N oldValue, N newValue) {
			updateLayout();
			surface.repaint(bounds);
		}

		@Override
		public void onRemoved(int index, N oldValue) {
			updateLayout();
			surface.repaint(bounds);
		}
		
		@Override
		public void close() {
			collapseListener.close();
			childListener.close();
		}
	}
}
