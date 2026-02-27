from math import sin, cos, pi, atan2

"""
8 8 145.783000 64.000000 141.417000
[16:32:18] [main/INFO] (autospeedrun) announcement: Press key W
9 8 145.783000 64.000000 141.515000
10 8 145.783000 64.000000 141.666000
11 8 145.783000 64.000000 141.847000
12 8 145.783000 64.000000 142.044000
13 8 145.783000 64.000000 142.249000
14 8 145.783000 64.000000 142.459000
15 8 145.783000 64.000000 142.672000
16 8 145.783000 64.000000 142.886000
17 8 145.783000 64.000000 143.101000
18 8 145.783000 64.000000 143.316000
19 8 145.783000 64.000000 143.532000
20 8 145.783000 64.000000 143.748000
21 8 145.783000 64.000000 143.963000
22 8 145.783000 64.000000 144.179000
23 8 145.783000 64.000000 144.395000
24 8 145.783000 64.000000 144.611000
25 8 145.783000 64.000000 144.827000
26 8 145.783000 64.000000 145.043000
27 8 145.783000 64.000000 145.258000
28 8 145.783000 64.000000 145.474000
[16:32:19] [main/INFO] (autospeedrun) announcement: Release key W
29 8 145.783000 64.000000 145.592000
30 8 145.783000 64.000000 145.657000
31 8 145.783000 64.000000 145.692000
32 8 145.783000 64.000000 145.711000
33 8 145.783000 64.000000 145.721000
34 8 145.783000 64.000000 145.727000
35 8 145.783000 64.000000 145.730000
36 8 145.783000 64.000000 145.730000
"""


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

        prev_v_x, prev_v_z = self.vx, self.vz
        prev_p_x, prev_p_z = self.x, self.z
        momentum_x = prev_v_x * slippery_multiplier * 0.91
        momentum_z = prev_v_z * slippery_multiplier * 0.91
        accel_x = 0.1 * movement_multiplier * effects_multiplier * (0.6/slippery_multiplier)**3 * sin(self.yaw)
        accel_z = 0.1 * movement_multiplier * effects_multiplier * (0.6/slippery_multiplier)**3 * cos(self.yaw)
        self.velo = momentum_x + accel_x, self.vy, momentum_z + accel_z
        self.pos = prev_p_x + momentum_x + accel_x, self.y, prev_p_z + momentum_z + accel_z

    def __repr__(self):
        return f"<Player({self.x:.6f}, {self.y:.6f}, {self.z:.6f}, {self.yaw * 180 / pi:.1f})>"


def main():
    for n in range(0, 70):
        p = Player(0, 0, 0)
        p.sneaking = True
        for i in range(900):
            if i == 0:
                p.set_fb_lr(1, 0)
            if i == n:
                p.set_fb_lr(0, 0)
            p.tick()
        print((n, p.pos[2]))

if __name__ == '__main__':
    main()
