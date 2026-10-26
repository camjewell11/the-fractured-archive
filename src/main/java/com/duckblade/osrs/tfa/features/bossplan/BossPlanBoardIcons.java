package com.duckblade.osrs.tfa.features.bossplan;

import com.duckblade.osrs.tfa.TfaConfig;
import com.duckblade.osrs.tfa.module.PluginLifecycleComponent;
import com.duckblade.osrs.tfa.util.RaidState;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BossPlanBoardIcons
	implements PluginLifecycleComponent
{

	private static final int SCRIPT_ID_TFA_PARTYLIST_ADDLINE = 10094;

	// row children are created by script10095, 0-8 are used by the game
	private static final int ROW_CHILD_NAME = 2;
	private static final int ROW_CHILD_MODE = 4;
	private static final int ROW_CHILD_FIRST_ICON = 9;
	private static final int MAX_ICONS = 3;

	private static final int ICON_GAP = 2;

	private final Client client;
	private final ClientThread clientThread;
	private final BossPlanSource bossPlanSource;

	private boolean needScanPartyList;

	@Override
	public boolean isEnabled(TfaConfig config, RaidState raidState)
	{
		return config.bossPlanBoardIcons();
	}

	@Override
	public void startUp()
	{
		needScanPartyList = true;
	}

	@Override
	public void shutDown()
	{
		clientThread.invokeLater(() -> scanPartyList(leader -> Collections.emptyList()));
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() == SCRIPT_ID_TFA_PARTYLIST_ADDLINE)
		{
			needScanPartyList = true;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (TfaConfig.CONFIG_GROUP.equals(e.getGroup()))
		{
			needScanPartyList = true;
		}
	}

	@Subscribe
	public void onClientTick(ClientTick e)
	{
		if (needScanPartyList)
		{
			scanPartyList(bossPlanSource::getPlan);
			needScanPartyList = false;
		}
	}

	private void scanPartyList(Function<String, List<PlannedBoss>> planLookup)
	{
		Widget partyList = client.getWidget(InterfaceID.TfaPartylist.LIST);
		Widget[] staticChildren;
		if (partyList == null || (staticChildren = partyList.getStaticChildren()) == null)
		{
			return;
		}

		for (Widget row : staticChildren)
		{
			Widget name = row.getChild(ROW_CHILD_NAME);
			if (name != null)
			{
				showPlan(row, planLookup.apply(Text.removeTags(name.getText())));
			}
		}
	}

	private void showPlan(Widget row, List<PlannedBoss> plan)
	{
		Widget mode = row.getChild(ROW_CHILD_MODE);
		if (mode == null)
		{
			return;
		}

		// the icons take over the mode column, as in the concept art
		mode.setHidden(!plan.isEmpty());

		int count = Math.min(plan.size(), MAX_ICONS);
		int iconHeight = row.getOriginalHeight();
		int iconWidth = iconHeight * 36 / 32;
		int fitWidth = (mode.getOriginalWidth() - ICON_GAP * (count - 1)) / Math.max(count, 1);
		if (iconWidth > fitWidth)
		{
			iconWidth = fitWidth;
			iconHeight = iconWidth * 32 / 36;
		}

		int x = mode.getOriginalX() + (mode.getOriginalWidth() - (iconWidth * count + ICON_GAP * (count - 1))) / 2;
		for (int i = 0; i < MAX_ICONS; i++)
		{
			if (i >= count)
			{
				Widget stale = row.getChild(ROW_CHILD_FIRST_ICON + i);
				if (stale != null)
				{
					stale.setHidden(true);
				}
				continue;
			}

			Widget icon = row.createChild(ROW_CHILD_FIRST_ICON + i, WidgetType.GRAPHIC);
			icon.setItemId(plan.get(i).getIconItemId());
			icon.setItemQuantityMode(ItemQuantityMode.NEVER);
			icon.setBorderType(1);
			icon.setOriginalX(x + i * (iconWidth + ICON_GAP));
			icon.setOriginalY(0);
			icon.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
			icon.setOriginalWidth(iconWidth);
			icon.setOriginalHeight(iconHeight);
			icon.revalidate();
		}
	}
}
