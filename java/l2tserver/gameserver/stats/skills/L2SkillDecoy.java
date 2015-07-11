/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2tserver.gameserver.stats.skills;

import l2tserver.gameserver.ai.CtrlIntention;
import l2tserver.gameserver.datatables.NpcTable;
import l2tserver.gameserver.idfactory.IdFactory;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.instance.L2DecoyInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.templates.StatsSet;
import l2tserver.gameserver.templates.chars.L2NpcTemplate;
import l2tserver.util.Rnd;

public class L2SkillDecoy extends L2Skill
{
	private final int _npcId;
	private final int _summonTotalLifeTime;
	
	public L2SkillDecoy(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_summonTotalLifeTime= set.getInteger("summonTotalLifeTime", 20000);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
			return;
		
		if (_npcId == 0)
			return;
		
		final L2PcInstance activeChar = (L2PcInstance) caster;
		
		if (activeChar.inObserverMode())
			return;
		
		if (activeChar.getPet() != null || activeChar.isMounted() || !activeChar.getSummons().isEmpty())
			return;
		
		L2NpcTemplate decoyTemplate = NpcTable.getInstance().getTemplate(_npcId);
		
		//TODO LasTravel, let's fix it by skill name, because for example Confusion Decoy have same npcIds as clone attak...
		if (getName().equalsIgnoreCase("Clone Attack"))
		{
			float angle = Rnd.get(1000);
			for (int i = 0; i < 3; i++)
			{
				final L2DecoyInstance decoy = new L2DecoyInstance(IdFactory.getInstance().getNextId(), decoyTemplate, activeChar, this);
				decoy.setCurrentHp(decoy.getMaxHp());
				decoy.setCurrentMp(decoy.getMaxMp());
				decoy.setHeading(activeChar.getHeading());
				activeChar.setDecoy(decoy);
				//L2World.getInstance().storeObject(Decoy);
				int x = Math.round(targets[0].getX() + (float)Math.cos(angle/1000*2*Math.PI) * 30);
				int y = Math.round(targets[0].getY() + (float)Math.sin(angle/1000*2*Math.PI) * 30);
				int z = targets[0].getZ() + 50;
				decoy.spawnMe(x, y, z);
				
				decoy.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targets[0]);
				
				angle += 1000 / 3;
			}
		}
		else
		{
			final L2DecoyInstance decoy = new L2DecoyInstance(IdFactory.getInstance().getNextId(), decoyTemplate, activeChar, this);
			decoy.setCurrentHp(decoy.getMaxHp());
			decoy.setCurrentMp(decoy.getMaxMp());
			decoy.setHeading(activeChar.getHeading());
			activeChar.setDecoy(decoy);
			//L2World.getInstance().storeObject(Decoy);
			decoy.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		}
	}
	
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
}