package com.duckblade.osrs.tfa.features.bossplan;

import com.duckblade.osrs.tfa.TfaConfig;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.util.Text;

/**
 * Looks up which bosses a party leader has advertised.
 * <p>
 * The game has no field for this, so plans can't be read from other clients yet.
 * Until a shared backend exists, only the local player's own plan is known,
 * which is enough to preview it on the lobby board while leading a party.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BossPlanSource
{

	private final Client client;
	private final TfaConfig config;

	public List<PlannedBoss> getPlan(String leaderName)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null ||
			!Text.standardize(localPlayer.getName()).equals(Text.standardize(leaderName)))
		{
			return Collections.emptyList();
		}

		return getLocalPlan();
	}

	public List<PlannedBoss> getLocalPlan()
	{
		return Stream.of(config.bossPlan1().getBoss(), config.bossPlan2().getBoss(), config.bossPlan3().getBoss())
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
