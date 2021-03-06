package org.chibitomo.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.chibitomo.plugin.Plugin;

public class Utils {
	public static Entity getEntityAt(Location loc) {
		World world = loc.getWorld();
		for (Entity e : world.getEntities()) {
			if (e instanceof LivingEntity) {
				continue;
			}
			Location eLoc = e.getLocation();
			if ((loc.getBlockX() == eLoc.getBlockX())
					&& (loc.getBlockY() == eLoc.getBlockY())
					&& (loc.getBlockZ() == eLoc.getBlockZ())) {
				return e;
			}
		}
		return null;
	}

	public static Location[] block2Locs(Block block, BlockFace face) {
		World world = block.getWorld();

		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();

		Location[] result = new Location[4];
		result[0] = new Location(world, x, y, z);
		result[1] = new Location(world, x, y, z);
		result[2] = new Location(world, x, y, z);
		result[3] = new Location(world, x, y, z);

		if (face.equals(BlockFace.NORTH)) {
			result[0].setX(x + 1);
			result[1].setX(x - 1);
			result[2].setZ(z + 1);
			result[3].setZ(z - 1);
		} else if (face.equals(BlockFace.SOUTH)) {
			result[0].setX(x + 1);
			result[1].setX(x - 1);
			result[2].setZ(z - 1);
			result[3].setZ(z + 1);
		} else if (face.equals(BlockFace.EAST)) {
			result[0].setX(x - 1);
			result[1].setZ(z - 1);
			result[2].setZ(z + 1);
			result[3].setX(x + 1);
		} else if (face.equals(BlockFace.WEST)) {
			result[0].setX(x + 1);
			result[1].setZ(z - 1);
			result[2].setZ(z + 1);
			result[3].setX(x - 1);
		}
		return result;
	}

	public static int blockFace2int(BlockFace face) {
		int result = 0;
		if (face == BlockFace.EAST) {
			result = 1;
		} else if (face == BlockFace.SOUTH) {
			result = 2;
		} else if (face == BlockFace.WEST) {
			result = 3;
		}
		return result;
	}

	public static String loc2Coord(Location loc, BlockFace face) {
		String coord = loc.getBlockX() + "," + loc.getBlockY() + ","
				+ loc.getBlockZ() + "," + blockFace2int(face);
		return coord;
	}

	public static BlockFace int2BlockFace(int i) {
		BlockFace result = BlockFace.NORTH;
		if (i == 1) {
			result = BlockFace.EAST;
		} else if (i == 2) {
			result = BlockFace.SOUTH;
		} else if (i == 3) {
			result = BlockFace.WEST;
		}
		return result;
	}

	public static Location coord2Loc(World world, String coord) {
		Integer[] c = coord2inta(coord);
		return new Location(world, c[0], c[1], c[2]);
	}

	public static Integer[] coord2inta(String coord) {
		String[] c = coord.split(",");
		int x = Integer.parseInt(c[0]);
		int y = Integer.parseInt(c[1]);
		int z = Integer.parseInt(c[2]);
		int f = Integer.parseInt(c[3]);
		return new Integer[] { x, y, z, f };
	}

	public static BlockFace coord2BlockFace(String coord) {
		Integer[] c = coord2inta(coord);
		return int2BlockFace(c[3]);
	}

	public static Location fLoc2BLoc(Location loc, BlockFace face) {
		if (loc == null) {
			throwNullException();
		}
		double x = loc.getBlockX();
		double z = loc.getBlockZ();
		if (face.equals(BlockFace.NORTH)) {
			z++;
		} else if (face.equals(BlockFace.SOUTH)) {
			z--;
		} else if (face.equals(BlockFace.EAST)) {
			x--;
		} else if (face.equals(BlockFace.WEST)) {
			x++;
		}
		return new Location(loc.getWorld(), x, loc.getBlockY(), z);
	}

	public static void throwNullException() {
		NullPointerException e = new NullPointerException();
		StackTraceElement[] stack = e.getStackTrace();
		stack = (StackTraceElement[]) ArrayUtils.subarray(stack, 2,
				stack.length);
		e.setStackTrace(stack);
		throw e;
	}

	public static void delay(Plugin plugin, final Object obj, String methodName) {
		delay(plugin, obj, methodName, 0);
	}

	public static void delay(final Plugin plugin, final Object obj,
			String methodName, int delay) {
		delay(plugin, obj, methodName, new Object[] {}, delay);
	}

	public static void delay(final Plugin plugin, final Object obj,
			String methodName, Object arg, int delay) {
		delay(plugin, obj, methodName, new Object[] { arg }, delay);
	}

	public static void delay(final Plugin plugin, final Object obj,
			final String methodName, final Object[] args, int delay) {
		final Method method;
		try {
			plugin.debug("Preparing delayed task: " + methodName);
			List<Class<?>> argsClassList = new ArrayList<Class<?>>();
			for (Object o : args) {
				argsClassList.add(o.getClass());
			}

			@SuppressWarnings("rawtypes")
			final Class[] argsTypes = new Class[argsClassList.size()];
			argsClassList.toArray(argsTypes);
			method = obj.getClass().getDeclaredMethod(methodName, argsTypes);

			plugin.getServer().getScheduler()
					.runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							try {
								plugin.debug("Run delayed task: " + methodName);
								method.setAccessible(true);
								method.invoke(obj, args);
							} catch (IllegalAccessException e) {
								plugin.error(e);
							} catch (IllegalArgumentException e) {
								plugin.error(e);
							} catch (InvocationTargetException e) {
								plugin.error(e);
							}
						}

					}, delay);
		} catch (NoSuchMethodException e) {
			plugin.error(e, "Cannot find method: " + methodName);
		}
	}

	public static void time(Plugin plugin, final Object obj, String str) {
		time(plugin, obj, str, 0, 20);
	}

	public static BukkitTask time(Plugin plugin, Object obj, String methodName,
			int delay, int period) {
		return time(plugin, obj, methodName, new Object[] {}, delay, period);
	}

	public static BukkitTask time(Plugin plugin, Object obj, String methodName,
			Object arg, int delay, int period) {
		return time(plugin, obj, methodName, new Object[] { arg }, delay,
				period);
	}

	public static BukkitTask time(final Plugin plugin, final Object obj,
			String methodName, final Object[] args, int delay, int period) {
		final Method method;
		try {
			List<Class<?>> argsClassList = new ArrayList<Class<?>>();
			for (Object o : args) {
				argsClassList.add(o.getClass());
			}

			@SuppressWarnings("rawtypes")
			final Class[] argsTypes = new Class[argsClassList.size()];
			argsClassList.toArray(argsTypes);
			method = obj.getClass().getDeclaredMethod(methodName, argsTypes);

			return plugin.getServer().getScheduler()
					.runTaskTimer(plugin, new Runnable() {
						@Override
						public void run() {
							try {
								method.setAccessible(true);
								method.invoke(obj, args);
							} catch (IllegalAccessException e) {
								plugin.error(e);
							} catch (IllegalArgumentException e) {
								plugin.error(e);
							} catch (InvocationTargetException e) {
								plugin.error(e);
							}
						}

					}, delay, period);
		} catch (NoSuchMethodException e) {
			plugin.error(e, "Cannot find method: " + methodName);
		}
		return null;
	}

	public static double getDist(Location loc1, Location loc2) {
		double x = Math.pow(loc1.getX() - loc2.getX(), 2);
		double y = Math.pow(loc1.getY() - loc2.getY(), 2);
		double z = Math.pow(loc1.getZ() - loc2.getZ(), 2);
		return Math.sqrt(x + y + z);
	}

	public static boolean isSomethingBeetween(LivingEntity entity,
			LivingEntity otherEntity) {
		return isSomethingBeetween(entity, otherEntity, new ArrayList<String>());
	}

	public static boolean isSomethingBeetween(LivingEntity entity,
			LivingEntity otherEntity, List<String> transpBlock) {
		Location loc = entity.getEyeLocation();
		Location otherLoc = otherEntity.getEyeLocation();
		Vector direction = getVector(loc, otherLoc);
		double y = entity.getEyeHeight();
		int toSlendermanDist = (int) getDist(loc, otherLoc);

		BlockIterator bIt = new BlockIterator(entity.getWorld(), entity
				.getLocation().toVector(), direction, y, toSlendermanDist);

		// TODO: Configurable transparent blocks.
		transpBlock.add("AIR");

		boolean somethingBetween = false;
		while (!somethingBetween && bIt.hasNext()) {
			Block block = bIt.next();
			somethingBetween = !transpBlock
					.contains(block.getType().toString());
		}
		return somethingBetween;
	}

	public static boolean canSee(Player p1, Player p2, double viewDist,
			int viewAngle) {
		Location p1EyeLoc = p1.getEyeLocation();
		Location p2EyeLoc = p2.getEyeLocation();
		double toSlendermanDist = Utils.getDist(p1EyeLoc, p2EyeLoc);

		Vector toSlendermanVect = getVector(p1EyeLoc, p2EyeLoc);

		Vector eyeDir = p1EyeLoc.getDirection();
		double angle = (eyeDir.angle(toSlendermanVect) / (2 * Math.PI)) * 360;

		boolean nothingBetween = !isSomethingBeetween(p1, p2);

		if ((toSlendermanDist <= viewDist) && nothingBetween
				&& (angle < viewAngle)) {
			return true;
		}
		return false;
	}

	private static Vector getVector(Location from, Location to) {
		float x = (float) (to.getX() - from.getX());
		float y = (float) (to.getY() - from.getY());
		float z = (float) (to.getZ() - from.getZ());
		return new Vector(x, y, z);
	}
}
