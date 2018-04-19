/*
 *  Copyright (c) 2018, Nicholas I
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.itemdragging;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.DraggingWidgetChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WidgetMenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "Item Dragging")
public class ItemDraggingPlugin extends Plugin
{
	private static final String LOCK = "Lock";
	private static final String UNLOCK = "Unlock";
	private static final String MENU_TARGET = "Item Dragging";

	private static final WidgetMenuOption FIXED_INVENTORY_TAB_LOCK = new WidgetMenuOption(LOCK,
			MENU_TARGET, WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB);

	private static final WidgetMenuOption FIXED_INVENTORY_TAB_UNLOCK = new WidgetMenuOption(UNLOCK,
			MENU_TARGET, WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB);

	private static final WidgetMenuOption RESIZABLE_INVENTORY_TAB_LOCK = new WidgetMenuOption(LOCK,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_TAB);

	private static final WidgetMenuOption RESIZABLE_INVENTORY_TAB_UNLOCK = new WidgetMenuOption(UNLOCK,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_TAB);

	private static final WidgetMenuOption RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_LOCK = new WidgetMenuOption(LOCK,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB);

	private static final WidgetMenuOption RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_UNLOCK = new WidgetMenuOption(UNLOCK,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB);

	@Inject
	private Client client;
	
	@Inject
	private ItemDraggingConfig config;

	@Inject
	private MenuManager menuManager;
	
	@Provides
	ItemDraggingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ItemDraggingConfig.class);
	}
	
	@Override
	protected void startUp() throws Exception
	{
		refreshInventoryTabOption();
	}
	
	@Override
	protected void shutDown() throws Exception
	{
		clearInventoryTabMenus();
	}
	
	private void clearInventoryTabMenus()
	{
		menuManager.removeManagedCustomMenu(FIXED_INVENTORY_TAB_LOCK);
		menuManager.removeManagedCustomMenu(FIXED_INVENTORY_TAB_UNLOCK);
		menuManager.removeManagedCustomMenu(RESIZABLE_INVENTORY_TAB_LOCK);
		menuManager.removeManagedCustomMenu(RESIZABLE_INVENTORY_TAB_UNLOCK);
		menuManager.removeManagedCustomMenu(RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_LOCK);
		menuManager.removeManagedCustomMenu(RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_UNLOCK);
	}
	
	private void refreshInventoryTabOption()
	{
		clearInventoryTabMenus();
		if (config.unlockInventoryDrag())
		{
			menuManager.addManagedCustomMenu(FIXED_INVENTORY_TAB_LOCK);
			menuManager.addManagedCustomMenu(RESIZABLE_INVENTORY_TAB_LOCK);
			menuManager.addManagedCustomMenu(RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_LOCK);
		}
		else
		{
			menuManager.addManagedCustomMenu(FIXED_INVENTORY_TAB_UNLOCK);
			menuManager.addManagedCustomMenu(RESIZABLE_INVENTORY_TAB_UNLOCK);
			menuManager.addManagedCustomMenu(RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_UNLOCK);
		}
	}
	
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("itemdragging"))
		{
			if (event.getKey().equals("unlockInventoryDrag"))
			{
				refreshInventoryTabOption();
			}
		}
	}
	
	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == WidgetID.INVENTORY_GROUP_ID)
		{
			System.out.println("Inventory Widget Loaded");
		}
	}
	
	@Subscribe
	public void onDraggingWidgetChanged(DraggingWidgetChanged event)
	{
		// is dragging widget and mouse button released
		if (event.isDraggingWidget() && client.getMouseCurrentButton() == 0)
		{
			Widget draggedWidget = client.getDraggedWidget();
			Widget draggedOnWidget = client.getDraggedOnWidget();
			if (draggedWidget != null && draggedOnWidget != null)
			{
				int draggedGroupId = WidgetInfo.TO_GROUP(draggedWidget.getId());
				int draggedOnGroupId = WidgetInfo.TO_GROUP(draggedOnWidget.getId());
				System.out.println(draggedWidget.getName());
				System.out.println(draggedOnWidget.getName());
			}
		}
	}
	
	@Subscribe
	public void onWidgetMenuOptionClicked(WidgetMenuOptionClicked event)
	{
		//When the lock/unlock menu button is pressed
		if (event.getWidget() == WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB
				|| event.getWidget() == WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_TAB
				|| event.getWidget() == WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB)
		{
			if (event.getMenuTarget().contains(MENU_TARGET))
			{
				System.out.println("Clicked on lock/unlock");
				config.unlockInventoryDrag(event.getMenuOption().equals(UNLOCK));
			}
		}
	}
}
