package com.duckblade.osrs.tfa.features.bossplan;

import com.duckblade.osrs.tfa.util.RaidRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ItemID;

@Getter
@RequiredArgsConstructor
public enum PlannedBoss
{

	NONE(null, -1),
	EZEKIEL(RaidRoom.EZEKIEL, ItemID.EZEKIELPET),
	BORK(RaidRoom.BORK, ItemID.BORKPET),
	MOIA(RaidRoom.MOIA, ItemID.MOIAPET),
	DREADNOTH(RaidRoom.DREADNOTH, ItemID.JELLYPET),
	DAGONHAI(RaidRoom.DAGONHAI, ItemID.DAGONHAIPET),
	XARTH(RaidRoom.XARTH, ItemID.XARTHPET),
	;

	private final RaidRoom room;
	private final int iconItemId;

	@Override
	public String toString()
	{
		return room == null ? "None" : room.getDisplayName();
	}
}
