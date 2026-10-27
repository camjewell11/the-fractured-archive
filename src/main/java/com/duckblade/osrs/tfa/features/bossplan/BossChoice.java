package com.duckblade.osrs.tfa.features.bossplan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Each boss in a raid is one of a fixed pair, so the config offers one dropdown per pair.
 */
public final class BossChoice
{

	private BossChoice()
	{
	}

	@Getter
	@RequiredArgsConstructor
	public enum First
	{
		NONE(null),
		EZEKIEL(PlannedBoss.EZEKIEL),
		BORK(PlannedBoss.BORK),
		;

		private final PlannedBoss boss;

		@Override
		public String toString()
		{
			return boss == null ? "None" : boss.toString();
		}
	}

	@Getter
	@RequiredArgsConstructor
	public enum Second
	{
		NONE(null),
		DREADNOTH(PlannedBoss.DREADNOTH),
		MOIA(PlannedBoss.MOIA),
		;

		private final PlannedBoss boss;

		@Override
		public String toString()
		{
			return boss == null ? "None" : boss.toString();
		}
	}

	@Getter
	@RequiredArgsConstructor
	public enum Third
	{
		NONE(null),
		XARTH(PlannedBoss.XARTH),
		DAGONHAI(PlannedBoss.DAGONHAI),
		;

		private final PlannedBoss boss;

		@Override
		public String toString()
		{
			return boss == null ? "None" : boss.toString();
		}
	}
}
