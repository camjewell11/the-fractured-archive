package com.duckblade.osrs.tfa.features.bossplan;

import com.duckblade.osrs.tfa.util.RaidRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlannedBoss
{

	EZEKIEL(RaidRoom.EZEKIEL, "ezekiel.png"),
	BORK(RaidRoom.BORK, "bork.png"),
	DREADNOTH(RaidRoom.DREADNOTH, "dreadnoth.png"),
	MOIA(RaidRoom.MOIA, "moia.png"),
	XARTH(RaidRoom.XARTH, "xarth.png"),
	DAGONHAI(RaidRoom.DAGONHAI, "dagonhai.png"),
	;

	private final RaidRoom room;
	private final String iconFile;

	@Override
	public String toString()
	{
		return room.getDisplayName();
	}
}
