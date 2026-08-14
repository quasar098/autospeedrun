package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.usercode.Dimension;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;

public enum Direction {
    WEST,
    EAST,
    NORTH,
    SOUTH;

    public enum Axis {
        X,
        Y,
        Z;

        public Axis perp1() {
            switch (this) {
                case X:
                    return Y;
                case Y:
                    return Z;
                case Z:
                default:
                    return X;
            }
        }

        public Axis perp2() {
            switch (this) {
                case X:
                    return Z;
                case Y:
                    return X;
                case Z:
                default:
                    return Y;
            }
        }

        public static BlockLocation makeBL(Axis axis, Dimension dim, long main, long perp1, long perp2) {
            switch (axis) {
                case X:
                    return new BlockLocation(dim, main, perp1, perp2);
                case Y:
                    return new BlockLocation(dim, perp2, main, perp1);
                case Z:
                default:
                    return new BlockLocation(dim, perp1, perp2, main);
            }
        }
    }
}
