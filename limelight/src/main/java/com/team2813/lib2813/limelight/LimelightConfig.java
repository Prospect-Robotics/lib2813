package com.team2813.lib2813.limelight;

import java.util.stream.Stream;
import static java.util.stream.Collectors.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import edu.wpi.first.networktables.NetworkTable;

public class LimelightConfig {
	private final NetworkTable table;
	LimelightConfig(Limelight limelight) {
		table = Objects.requireNonNull(limelight).networkTable();
	}

	public Optional<LedMode> getLedMode() {
		return LedMode.fromId(table.getEntry("ledMode").getInteger(-1));
	}

	public void setLedMode(LedMode mode) {
		table.getEntry("ledMode").setInteger(mode.getID());
	}

	public Optional<CamMode> getCamMode() {
		return CamMode.fromId(table.getEntry("camMode").getInteger(-1));
	}

	public void setCamMode(CamMode mode) {
		table.getEntry("camMode").setInteger(mode.getID());
	}

	public Optional<Snapshot> getSnapshot() {
		return Snapshot.fromId(table.getEntry("snapshot").getInteger(-1));
	}

	public void setSnapshot(Snapshot snapshot) {
		table.getEntry("snapshot").setInteger(snapshot.getID());
	}

	public Optional<StreamMode> getStreamMode() {
		return StreamMode.fromId(table.getEntry("stream").getInteger(-1));
	}

	public void setStreamMode(StreamMode mode) {
		table.getEntry("stream").setInteger(mode.getID());
	}

	/**
	 * Sets the crop rectangle for the limelight
	 * @param crop the array describing the crop
	 */
	public void setCrop(double[] crop) {
		table.getEntry("crop").setDoubleArray(crop);
	}

	public static enum LedMode {
		/** default pipeline value */
		DEFAULT(0),
		/** force to off */
		OFF(1),
		/** force to blink */
		BLINK(2),
		/** force to on */
		ON(3);
		private final long id;
		private static Map<Long, LedMode> map =
			Stream.of(values()).collect(toMap(LedMode::getID,(k) -> k, (o, n) -> n));
		LedMode(long id) {
			this.id = id;
		}
		private long getID() {
			return id;
		}
		private static Optional<LedMode> fromId(long id) {
			return Optional.ofNullable(map.get(id));
		}
	}

	public static enum CamMode {
		VISION(0),
		DRIVER_CAMERA(1);
		private final long id;
		private static Map<Long, CamMode> map =
			Stream.of(values()).collect(toMap(CamMode::getID,(k) -> k, (o, n) -> n));
		CamMode(long id) {
			this.id = id;
		}
		private long getID() {
			return id;
		}
		private static Optional<CamMode> fromId(long id) {
			return Optional.ofNullable(map.get(id));
		}
	}

	public static enum Snapshot {
		RESET(0),
		TAKE_ONE(1);
		private final long id;
		private static Map<Long, Snapshot> map =
			Stream.of(values()).collect(toMap(Snapshot::getID,(k) -> k, (o, n) -> n));
		Snapshot(long id) {
			this.id = id;
		}
		private long getID() {
			return id;
		}
		private static Optional<Snapshot> fromId(long id) {
			return Optional.ofNullable(map.get(id));
		}
	}

	public static enum StreamMode {
		STANDARD(0),
		MAIN(1),
		SECONDARY(2);
		private final long id;
		private static Map<Long, StreamMode> map =
			Stream.of(values()).collect(toMap(StreamMode::getID, (k) -> k, (o, n) -> n));
		StreamMode(long id) {
			this.id = id;
		}
		private long getID() {
			return id;
		}
		private static Optional<StreamMode> fromId(long id) {
			return Optional.ofNullable(map.get(id));
		}
	}
}
