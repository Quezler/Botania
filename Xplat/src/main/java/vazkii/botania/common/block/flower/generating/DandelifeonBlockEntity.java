/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.block.block_entity.CellularBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class DandelifeonBlockEntity extends GeneratingFlowerBlockEntity {
	private static final int RANGE = 12;
	private static final int SPEED = 10;
//	private static final int MAX_GENERATIONS = 100;
	private static final int MAX_MANA_GENERATIONS = 100;
	private static final int MANA_PER_GEN = 60;

	private static final int[][] ADJACENT_BLOCKS = new int[][] {
			{ -1, -1 },
			{ -1, +0 },
			{ -1, +1 },
			{ +0, +1 },
			{ +1, +1 },
			{ +1, +0 },
			{ +1, -1 },
			{ +0, -1 }
	};

	public DandelifeonBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaFlowerBlocks.DANDELIFEON, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();

		if (!getLevel().isClientSide) {
			if (ticksExisted % SPEED == 0 && getLevel().hasNeighborSignal(getBlockPos())) {
				runSimulation();
			}
		}
	}

	private void runSimulation() {
		int[][] table = getCellTable();
		List<int[]> changes = new ArrayList<>();
		boolean wipe = false;

		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[0].length; j++) {
				int gen = table[i][j];
				int adj = getAdjCells(table, i, j);

				int newVal = gen;
				if (adj < 2 || adj > 3) {
					newVal = -1;
				} else {
					if (adj == 3 && gen == -1) {
						newVal = getSpawnCellGeneration(table, i, j);
					} else if (gen > -1) {
						newVal = gen + 1;
					}
				}

				int xdist = Math.abs(i - RANGE);
				int zdist = Math.abs(j - RANGE);
				int allowDist = 1;
				if (xdist <= allowDist && zdist <= allowDist && newVal > -1) {
					gen = newVal;
					newVal = gen == 1 ? -1 : -2;
				}

				if (newVal != gen) {
					changes.add(new int[] { i, j, newVal, gen });
					if (newVal == -2) {
						wipe = true;
					}
				}
			}
		}

		BlockPos pos = getEffectivePos();

		for (int[] change : changes) {
			BlockPos pos_ = pos.offset(-RANGE + change[0], 0, -RANGE + change[1]);
			int val = change[2];
			if (val != -2 && wipe) {
				val = -1;
			}

			int old = change[3];

			setBlockForGeneration(pos_, val, old);
		}
	}

	private int[][] getCellTable() {
		int diam = RANGE * 2 + 1;
		int[][] table = new int[diam][diam];

		BlockPos pos = getEffectivePos();

		for (int i = 0; i < diam; i++) {
			for (int j = 0; j < diam; j++) {
				BlockPos pos_ = pos.offset(-RANGE + i, 0, -RANGE + j);
				table[i][j] = getCellGeneration(pos_);
			}
		}

		return table;
	}

	private int getCellGeneration(BlockPos pos) {
		BlockEntity tile = getLevel().getBlockEntity(pos);
		if (tile instanceof CellularBlockEntity cell) {
			return cell.isSameFlower(this) ? cell.getGeneration() : 0;
		}

		return -1;
	}

	private int getAdjCells(int[][] table, int x, int z) {
		int count = 0;
		for (int[] shift : ADJACENT_BLOCKS) {
			int xp = x + shift[0];
			int zp = z + shift[1];
			if (!isOffBounds(table, xp, zp)) {
				int gen = table[xp][zp];
				if (gen >= 0) {
					count++;
				}
			}
		}

		return count;
	}

	private int getSpawnCellGeneration(int[][] table, int x, int z) {
		int max = -1;
		for (int[] shift : ADJACENT_BLOCKS) {
			int xp = x + shift[0];
			int zp = z + shift[1];
			if (!isOffBounds(table, xp, zp)) {
				int gen = table[xp][zp];
				if (gen > max) {
					max = gen;
				}
			}
		}

		return max == -1 ? -1 : max + 1;
	}

	boolean isOffBounds(int[][] table, int x, int z) {
		return x < 0 || z < 0 || x >= table.length || z >= table[0].length;
	}

	void setBlockForGeneration(BlockPos pos, int gen, int prevGen) {
		Level world = getLevel();
		BlockState stateAt = world.getBlockState(pos);
		BlockEntity tile = world.getBlockEntity(pos);
		if (gen == -2) {
			int val = Math.min(MAX_MANA_GENERATIONS, prevGen) * MANA_PER_GEN;
			addMana(val);
		} else if (stateAt.is(BotaniaBlocks.cellBlock)) {
			if (gen < 0) {
				world.removeBlock(pos, false);
			} else {
				((CellularBlockEntity) tile).setGeneration(this, gen);
			}
		} else if (gen >= 0 && stateAt.isAir()) {
			world.setBlockAndUpdate(pos, BotaniaBlocks.cellBlock.defaultBlockState());
			tile = world.getBlockEntity(pos);
			((CellularBlockEntity) tile).setGeneration(this, gen);
		}
	}

	@Override
	public RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public RadiusDescriptor getSecondaryRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), 1);
	}

	@Override
	public int getMaxMana() {
		return 50000;
	}

	@Override
	public int getColor() {
		return 0x9c0a7e;
	}

}
