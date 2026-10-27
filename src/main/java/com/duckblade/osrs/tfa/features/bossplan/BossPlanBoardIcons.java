package com.duckblade.osrs.tfa.features.bossplan;

import com.duckblade.osrs.tfa.TfaConfig;
import com.duckblade.osrs.tfa.module.PluginLifecycleComponent;
import com.duckblade.osrs.tfa.util.RaidState;
import java.awt.image.BufferedImage;
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
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.ImageUtil;
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
	private static final int ROW_CHILD_FIRST_TILE = 9;
	private static final int ROW_CHILD_FIRST_ICON = 12;
	private static final int MAX_ICONS = 3;

	// icons are pre-scaled to fit the row and drawn at their native size
	private static final int SPRITE_ID_BASE = -38_600;
	private static final int ICON_SIZE = 18;
	private static final int TILE_WIDTH = 22;
	private static final int TILE_HEIGHT = 20;
	private static final int TILE_GAP = 4;
	private static final int TILE_COLOUR = 0x7d7058;

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
		clientThread.invokeLater(() ->
		{
			for (PlannedBoss boss : PlannedBoss.values())
			{
				registerSprite(boss, ImageUtil.loadImageResource(BossPlanBoardIcons.class, boss.getIconFile()));
			}
			needScanPartyList = true;
		});
	}

	@Override
	public void shutDown()
	{
		clientThread.invokeLater(() ->
		{
			scanPartyList(leader -> Collections.emptyList());
			for (PlannedBoss boss : PlannedBoss.values())
			{
				client.getSpriteOverrides().remove(spriteId(boss));
			}
			client.getWidgetSpriteCache().reset();
		});
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

	private void registerSprite(PlannedBoss boss, BufferedImage image)
	{
		BufferedImage icon = ImageUtil.resizeImage(image, ICON_SIZE, ICON_SIZE);
		client.getSpriteOverrides().put(spriteId(boss), ImageUtil.getImageSpritePixels(icon, client));
		client.getWidgetSpriteCache().reset();
	}

	private static int spriteId(PlannedBoss boss)
	{
		return SPRITE_ID_BASE - boss.ordinal();
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
		int x = mode.getOriginalX() + (mode.getOriginalWidth() - (TILE_WIDTH * count + TILE_GAP * (count - 1))) / 2;
		for (int i = 0; i < MAX_ICONS; i++)
		{
			if (i >= count || !client.getSpriteOverrides().containsKey(spriteId(plan.get(i))))
			{
				hideChild(row, ROW_CHILD_FIRST_TILE + i);
				hideChild(row, ROW_CHILD_FIRST_ICON + i);
				continue;
			}

			int tileX = x + i * (TILE_WIDTH + TILE_GAP);

			Widget tile = row.createChild(ROW_CHILD_FIRST_TILE + i, WidgetType.RECTANGLE);
			tile.setFilled(true);
			tile.setTextColor(TILE_COLOUR);
			place(tile, tileX, TILE_WIDTH, TILE_HEIGHT);

			Widget icon = row.createChild(ROW_CHILD_FIRST_ICON + i, WidgetType.GRAPHIC);
			icon.setSpriteId(spriteId(plan.get(i)));
			place(icon, tileX + (TILE_WIDTH - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE);
		}
	}

	private static void place(Widget w, int x, int width, int height)
	{
		w.setOriginalX(x);
		w.setOriginalY(0);
		w.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		w.setOriginalWidth(width);
		w.setOriginalHeight(height);
		w.revalidate();
	}

	private static void hideChild(Widget row, int index)
	{
		Widget child = row.getChild(index);
		if (child != null)
		{
			child.setHidden(true);
		}
	}
}
