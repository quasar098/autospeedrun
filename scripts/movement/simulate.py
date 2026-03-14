from math import sin, cos, pi, atan2

data = """
0 18.405834 64.000000 -2.786372
1 18.405834 64.000000 -2.786372
2 18.405834 64.000000 -2.786372
3 18.405834 64.000000 -2.786372
4 18.405834 64.000000 -2.786372
5 18.405834 64.000000 -2.786372
6 18.405834 64.000000 -2.786372
7 18.405834 64.000000 -2.786372
8 18.405834 64.000000 -2.786372
9 18.405834 64.000000 -2.786372
10 18.405834 64.000000 -2.786372
11 18.405834 64.000000 -2.786372
[19:09:54] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:54] [main/INFO] (autospeedrun) announcement: Press key W
[19:09:54] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
12 18.405750 64.000000 -2.688372
[19:09:54] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
13 18.405724 64.000000 -2.605464
14 18.405640 64.000000 -2.462197
15 18.405555 64.000000 -2.285973
[19:09:54] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:54] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
16 18.405471 64.000000 -2.091754
[19:09:54] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
17 18.405445 64.000000 -1.956311
18 18.405361 64.000000 -1.784359
19 18.405276 64.000000 -1.592473
[19:09:54] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:54] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
20 18.405192 64.000000 -1.389704
[19:09:54] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
21 18.405166 64.000000 -1.249591
22 18.405082 64.000000 -1.075090
23 18.404997 64.000000 -0.881812
[19:09:55] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:55] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
24 18.404913 64.000000 -0.678283
[19:09:55] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
25 18.404887 64.000000 -0.537756
26 18.404803 64.000000 -0.363028
27 18.404718 64.000000 -0.169627
[19:09:55] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:55] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
28 18.404633 64.000000 0.033971
[19:09:55] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
29 18.404608 64.000000 0.174535
30 18.404524 64.000000 0.349283
31 18.404439 64.000000 0.542695
[19:09:55] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:55] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
32 18.404354 64.000000 0.746298
[19:09:55] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
33 18.404329 64.000000 0.886865
34 18.404245 64.000000 1.061615
35 18.404160 64.000000 1.255028
[19:09:55] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:55] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
36 18.404075 64.000000 1.458632
[19:09:55] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
37 18.404050 64.000000 1.599200
38 18.403965 64.000000 1.773950
39 18.403881 64.000000 1.967363
[19:09:55] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:55] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
40 18.403796 64.000000 2.170967
[19:09:55] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
41 18.403771 64.000000 2.311534
42 18.403686 64.000000 2.486284
43 18.403602 64.000000 2.679698
[19:09:56] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:56] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
44 18.403517 64.000000 2.883301
[19:09:56] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
45 18.403492 64.000000 3.023869
46 18.403407 64.000000 3.198619
47 18.403323 64.000000 3.392032
[19:09:56] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:56] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
48 18.403238 64.000000 3.595636
[19:09:56] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
49 18.403213 64.000000 3.736204
50 18.403128 64.000000 3.910954
51 18.403044 64.000000 4.104367
[19:09:56] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:56] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
52 18.402959 64.000000 4.307971
[19:09:56] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
53 18.402934 64.000000 4.448538
54 18.402849 64.000000 4.623288
55 18.402765 64.000000 4.816702
[19:09:56] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:56] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
56 18.402680 64.000000 5.020305
[19:09:56] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
57 18.402655 64.000000 5.160873
58 18.402570 64.000000 5.335623
59 18.402486 64.000000 5.529036
[19:09:56] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:56] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
60 18.402401 64.000000 5.732640
[19:09:56] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
61 18.402376 64.000000 5.873208
62 18.402291 64.000000 6.047958
63 18.402207 64.000000 6.241371
[19:09:57] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:57] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
64 18.402122 64.000000 6.444975
[19:09:57] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
65 18.402097 64.000000 6.585542
66 18.402012 64.000000 6.760292
67 18.401928 64.000000 6.953706
[19:09:57] [main/INFO] (Minecraft) [STDOUT]: Crouching
[19:09:57] [main/INFO] (autospeedrun) announcement: Press key LEFT_SHIFT
68 18.401843 64.000000 7.157309
[19:09:57] [main/INFO] (autospeedrun) announcement: Release key LEFT_SHIFT
69 18.401818 64.000000 7.297877
70 18.401733 64.000000 7.472627
[19:09:57] [main/INFO] (autospeedrun) announcement: Release key W
71 18.401733 64.000000 7.568040
72 18.401733 64.000000 7.620136
73 18.401733 64.000000 7.648580
74 18.401733 64.000000 7.664111
75 18.401733 64.000000 7.672591
76 18.401733 64.000000 7.677221
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
        self.sneaking_p = False

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

        if self.sneaking_p:
            movement_state = 0.3  # sneaking

        if self.fb == 1 and self.sprinting:
            movement_state *= 1.3  # sprinting

        if self.fb == self.lr == 0:
            movement_state = 0.0  # stopping

        movement_multiplier = movement_state * 0.98  # todo 45 strafes

        momentum_x = self.vx * slippery_multiplier * 0.91
        momentum_z = self.vz * slippery_multiplier * 0.91
        accel_x = 0.1 * movement_multiplier * effects_multiplier * (0.6/slippery_multiplier)**3 * sin(self.yaw)
        accel_z = 0.1 * movement_multiplier * effects_multiplier * (0.6/slippery_multiplier)**3 * cos(self.yaw)
        self.velo = momentum_x + accel_x, self.vy, momentum_z + accel_z
        self.pos = self.x + self.vx, self.y, self.z + self.vz
        self.sneaking_p = self.sneaking

    def __repr__(self):
        return f"<Player({self.x:.6f}, {self.y:.6f}, {self.z:.6f}, {self.yaw * 180 / pi:.1f})>"


def main():
    p = Player(*[*map(float, data[0].split(" "))][1:])
    for tick in range(77):
        if tick == 0:
            print("doing sprinting")
            p.set_sprinting(False)
        p.tick()
        print(f"{tick} {p.x:.6f} {p.y:.6f} {p.z:.6f}")
        # print(fr"\left({tick},{p.z:.6f}\right)")
        if tick == 11:
            p.set_fb_lr(1, 0)
            print("press w")
        if tick == 70:
            p.set_fb_lr(0, 0)
            print("release w")
        if 70 > tick >= 11 and tick % 4 == 3:
            p.set_sneaking(True)
            print("sneaking for this tick")
        else:
            p.set_sneaking(False)

if __name__ == '__main__':
    main()
