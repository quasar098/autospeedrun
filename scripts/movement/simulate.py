from math import sin, cos, pi, atan2

data = """
8 8 186.359000 63.000000 161.240000 186.359460 63.000000 161.239547
[18:30:51] [main/INFO] (autospeedrun) announcement: Press key W
9 8 186.360000 63.000000 161.338000 186.359526 63.000000 161.337547
10 8 186.360000 63.000000 161.489000 186.359591 63.000000 161.489055
11 8 186.360000 63.000000 161.670000 186.359657 63.000000 161.669778
[18:30:51] [main/INFO] (Minecraft) [STDOUT]: Crouching
[18:30:51] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
12 8 186.360000 63.000000 161.866000 186.359723 63.000000 161.866453
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
13 8 186.360000 63.000000 162.003000 186.359743 63.000000 162.003237
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
14 8 186.360000 63.000000 162.107000 186.359762 63.000000 162.107322
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
15 8 186.360000 63.000000 162.194000 186.359782 63.000000 162.193552
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
16 8 186.360000 63.000000 162.270000 186.359802 63.000000 162.270033
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
17 8 186.360000 63.000000 162.341000 186.359822 63.000000 162.341192
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
18 8 186.360000 63.000000 162.409000 186.359841 63.000000 162.409445
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
19 8 186.360000 63.000000 162.476000 186.359861 63.000000 162.476111
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
20 8 186.360000 63.000000 162.542000 186.359881 63.000000 162.541911
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
21 8 186.360000 63.000000 162.607000 186.359901 63.000000 162.607238
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
22 8 186.360000 63.000000 162.672000 186.359920 63.000000 162.672306
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
23 8 186.360000 63.000000 162.737000 186.359940 63.000000 162.737233
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
24 8 186.360000 63.000000 162.802000 186.359960 63.000000 162.802083
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
25 8 186.360000 63.000000 162.867000 186.359980 63.000000 162.866892
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
26 8 186.360000 63.000000 162.932000 186.359999 63.000000 162.931677
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
27 8 186.360000 63.000000 162.996000 186.360019 63.000000 162.996450
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
28 8 186.360000 63.000000 163.061000 186.360039 63.000000 163.061216
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
29 8 186.360000 63.000000 163.126000 186.360058 63.000000 163.125978
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
30 8 186.360000 63.000000 163.191000 186.360078 63.000000 163.190738
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
31 8 186.360000 63.000000 163.255000 186.360098 63.000000 163.255497
[18:30:52] [main/INFO] (Minecraft) [STDOUT]: Crouching
32 8 186.360000 63.000000 163.320000 186.360118 63.000000 163.320256
[18:30:53] [main/INFO] (Minecraft) [STDOUT]: Crouching
33 8 186.360000 63.000000 163.385000 186.360137 63.000000 163.385014
[18:30:53] [main/INFO] (Minecraft) [STDOUT]: Crouching
34 8 186.360000 63.000000 163.450000 186.360157 63.000000 163.449772
[18:30:53] [main/INFO] (Minecraft) [STDOUT]: Crouching
35 8 186.360000 63.000000 163.515000 186.360177 63.000000 163.514529
[18:30:53] [main/INFO] (Minecraft) [STDOUT]: Crouching
36 8 186.360000 63.000000 163.579000 186.360197 63.000000 163.579287
[18:30:53] [main/INFO] (Minecraft) [STDOUT]: Crouching
37 8 186.360000 63.000000 163.644000 186.360216 63.000000 163.644045
[18:30:53] [main/INFO] (Minecraft) [STDOUT]: Crouching
38 8 186.360000 63.000000 163.709000 186.360236 63.000000 163.708803
[18:30:53] [main/INFO] (Minecraft) [STDOUT]: Crouching
39 8 186.360000 63.000000 163.774000 186.360256 63.000000 163.773560
[18:30:53] [main/INFO] (Minecraft) [STDOUT]: Crouching
40 8 186.360000 63.000000 163.838000 186.360275 63.000000 163.838318
[18:30:53] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
41 8 186.360000 63.000000 163.903000 186.360295 63.000000 163.903076
42 8 186.360000 63.000000 164.036000 186.360361 63.000000 164.036434
43 8 186.360000 63.000000 164.207000 186.360427 63.000000 164.207247
44 8 186.360000 63.000000 164.399000 186.360493 63.000000 164.398511
45 8 186.361000 63.000000 164.601000 186.360558 63.000000 164.600941
46 8 186.361000 63.000000 164.809000 186.360624 63.000000 164.809468
47 8 186.361000 63.000000 165.021000 186.360690 63.000000 165.021323
48 8 186.361000 63.000000 165.235000 186.360756 63.000000 165.234997
[18:30:53] [main/INFO] (autospeedrun) announcement: Release key W
49 8 186.361000 63.000000 165.352000 186.360756 63.000000 165.351662
50 8 186.361000 63.000000 165.415000 186.360756 63.000000 165.415362
51 8 186.361000 63.000000 165.450000 186.360756 63.000000 165.450141
52 8 186.361000 63.000000 165.469000 186.360756 63.000000 165.469131
53 8 186.361000 63.000000 165.479000 186.360756 63.000000 165.479500
54 8 186.361000 63.000000 165.485000 186.360756 63.000000 165.485161
55 8 186.361000 63.000000 165.488000 186.360756 63.000000 165.488252
56 8 186.361000 63.000000 165.488000 186.360756 63.000000 165.488252
""".strip().splitlines(keepends=False)


class Player:
    def __init__(self, x, y, z, yaw: float = 0, sprinting: bool = False):
        self.x, self.y, self.z = x, y, z
        self.vx, self.vy, self.vz = 0, 0, 0

        # player movement keys and direction
        self.fb, self.lr = 0, 0
        self.yaw = yaw  # radians
        self.sprinting = sprinting
        self.sneaking = False

    @property
    def pos(self):
        return self.x, self.y, self.z

    @pos.setter
    def pos(self, value):
        self.x, self.y, self.z = value

    @property
    def velo(self):
        return self.vx, self.vy, self.vz

    @velo.setter
    def velo(self, value):
        self.vx, self.vy, self.vz = value

    def set_fb_lr(self, fb, lr) -> "Player":
        # f=1 b=-1 l=? r=?
        assert len({fb, lr, -1, 1, 0}) == 3
        self.fb, self.lr = fb, lr
        return self

    def set_yaw(self, yaw):
        self.yaw = yaw
        return self

    def set_sprinting(self, sprinting):
        self.sprinting = sprinting
        return self

    def set_sneaking(self, sneaking):
        self.sneaking = sneaking
        return self

    def tick(self) -> None:
        slippery_multiplier = 0.6  # default = 0.6

        effects_multiplier = 1.0

        movement_state = 1.0  # walking

        if self.fb == 1 and self.sprinting:
            movement_state = 1.3  # sprinting

        if self.sneaking:
            movement_state = 0.3  # sneaking

        if self.fb == self.lr == 0:
            movement_state = 0.0  # stopping

        movement_multiplier = movement_state * 0.98  # todo 45 strafes

        momentum_x = self.vz * slippery_multiplier * 0.91
        momentum_z = self.vx * slippery_multiplier * 0.91
        accel_x = 0.1 * movement_multiplier * effects_multiplier * (0.6/slippery_multiplier)**3 * sin(self.yaw)
        accel_z = 0.1 * movement_multiplier * effects_multiplier * (0.6/slippery_multiplier)**3 * cos(self.yaw)
        self.velo = momentum_x + accel_x, self.vy, momentum_z + accel_z
        self.pos = self.x + self.vx + accel_x, self.y, self.z + self.vy + accel_z

    def __repr__(self):
        return f"<Player({self.x:.6f}, {self.y:.6f}, {self.z:.6f}, {self.yaw * 180 / pi:.1f})>"


def main():
    p = Player(186.360231, 63.000000, 163.768621)
    c2 = False
    for line in data:
        if line.startswith("["):
            if 'STDOUT' in line:
                if 'Crouching' in line:
                    c2 = True
                else:
                    raise NotImplementedError(line)
        else:
            p.tick()
            p.set_sneaking(c2)
            c2 = False
            print(p.pos, p.sprinting, p.sneaking, c2, line.split(" ")[-3:])

if __name__ == '__main__':
    main()
