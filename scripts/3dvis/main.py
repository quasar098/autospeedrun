import pygame
from math import tan, sin, cos, pi
from enum import Enum
from typing import Sequence
from math import ceil, floor

pygame.init()

WIDTH, HEIGHT = 1280, 720
NO_COLOR = (255, 127, 255)
FRAMERATE = 100
MOUSE_SENS = 0.002
FLY_SPEED = 8
FOV = 103

screen = pygame.display.set_mode([WIDTH, HEIGHT])
clock = pygame.time.Clock()
font = pygame.font.Font("./minecraftia.ttf", size=16)


def project(a, c, t):
    ez = 1/tan((FOV / 180 * pi)/2)
    sinx, siny, sinz = sin(t[0]), sin(t[1]), sin(t[2])
    cosx, cosy, cosz = cos(t[0]), cos(t[1]), cos(t[2])
    x, y, z = c[0] - a[0], a[1] - c[1], a[2] - c[2]
    dx = cosy*(sinz*y + cosz*x) - siny*z
    dy = sinx*(cosy*z + siny*(sinz*y + cosz*x)) + cosx*(cosz*y - sinz*x)
    dz = cosx*(cosy*z + siny*(sinz*y + cosz*x)) - sinx*(cosz*y - sinz*x)
    if dz <= 0:
        return None
    return (ez/dz*dx) * WIDTH/2 + WIDTH/2, HEIGHT/2 - (ez/dz*dy) * WIDTH/2


class FaceDirection(Enum):
    UP = 0  # up y
    EAST = 1  # pos x
    SOUTH = 2  # pos z


faces_to_draw: list["Polygon3D"] = []


class Polygon3D:
    def __init__(self, verts: list[Sequence[float]], color: Sequence[int] = NO_COLOR, wireframe: bool = False,
                 z_override: int = None):
        self.verts = verts
        self.wireframe = wireframe
        self.color = color
        self.z_override = z_override

    def get_center_pos(self) -> Sequence[float]:
        return [sum(v[i] for v in self.verts)/len(self.verts) for i in range(3)]

    def do_draw_now(self, cam_pos, cam_rot):
        verts = []
        for vert in self.verts:
            vert_projected = project(vert, cam_pos, cam_rot)
            if vert_projected is not None:
                verts.append(vert_projected)
        if self.wireframe:
            if len(verts) < 2:
                return
            for a, b in zip(verts, verts[1:] + verts[:1]):
                if distance(a, b) > WIDTH * 10:
                    return
            pygame.draw.lines(screen, self.color, True, verts, width=2)
        else:
            if len(verts) < 3:
                return
            total = 0
            for i in range(len(verts)):
                total += verts[i][0]*verts[(i+1) % len(verts)][1]
                total -= verts[i][1]*verts[(i+1) % len(verts)][0]
            if 0.5 * abs(total) > HEIGHT * WIDTH:
                return
            pygame.draw.polygon(screen, self.color, verts)

    def queue_draw(self):
        faces_to_draw.append(self)


class BlockFace(Polygon3D):
    def __init__(self, xyz: Sequence[int], dir_: FaceDirection, color: Sequence[int] = NO_COLOR, wireframe: bool = False):
        self.dir = dir_
        self.x, self.y, self.z = xyz
        if self.dir == FaceDirection.UP:
            v1 = [self.x+1, self.y+1, self.z+1]
            v2 = [self.x+1, self.y+1, self.z]
            v3 = [self.x, self.y+1, self.z]
            v4 = [self.x, self.y+1, self.z+1]
        elif self.dir == FaceDirection.EAST:
            v1 = [self.x+1, self.y+1, self.z+1]
            v2 = [self.x+1, self.y+1, self.z]
            v3 = [self.x+1, self.y, self.z]
            v4 = [self.x+1, self.y, self.z+1]
        else:  # self.dir == FaceDirection.SOUTH
            v1 = [self.x+1, self.y+1, self.z+1]
            v2 = [self.x+1, self.y, self.z+1]
            v3 = [self.x, self.y, self.z+1]
            v4 = [self.x, self.y+1, self.z+1]
        verts = [v1, v2, v3, v4]
        super().__init__(verts, color=color, wireframe=wireframe)

    def __eq__(self, other):
        return isinstance(other, BlockFace) and (other.x, other.y, other.z, other.dir) == (self.x, self.y, self.z, self.dir)


class Block:
    def __init__(self, xyz: Sequence[int], block_name: str, wireframe: bool = False):
        self.xyz = xyz
        self.block_name = block_name
        self.wireframe = wireframe

    def get_colors(self) -> tuple[Sequence[int], Sequence[int], Sequence[int]]:
        # top,side,bottom
        if self.block_name == "grass_block":
            return (103, 162, 90), (96, 67, 45), (96, 67, 45)
        return NO_COLOR, NO_COLOR, NO_COLOR

    def get_faces(self):
        xyz = self.xyz
        top, side, bottom = self.get_colors()
        return [
            BlockFace(xyz, FaceDirection.UP, top, self.wireframe),
            BlockFace(xyz, FaceDirection.SOUTH, side, self.wireframe),
            BlockFace(xyz, FaceDirection.EAST, side, self.wireframe),
            BlockFace([xyz[0], xyz[1] - 1, xyz[2]], FaceDirection.UP, bottom, self.wireframe),
            BlockFace([xyz[0], xyz[1], xyz[2] - 1], FaceDirection.SOUTH, side, self.wireframe),
            BlockFace([xyz[0] - 1, xyz[1], xyz[2]], FaceDirection.EAST, side, self.wireframe)
        ]

    def queue_draw(self):
        for face in self.get_faces():
            face.queue_draw()


class PlayerHitbox:
    def __init__(self, xyz: Sequence[float], yaw: float, pitch: float):
        self.x, self.y, self.z = x, y, z = xyz
        self.yaw = yaw
        self.pitch = pitch
        self.bottom_face = Polygon3D([
            [x-0.3, y, z-0.3],
            [x-0.3, y, z+0.3],
            [x+0.3, y, z+0.3],
            [x+0.3, y, z-0.3],
        ], color=(255, 255, 255), wireframe=True)
        self.top_face = Polygon3D([
            [x-0.3, y+1.8, z-0.3],
            [x-0.3, y+1.8, z+0.3],
            [x+0.3, y+1.8, z+0.3],
            [x+0.3, y+1.8, z-0.3],
        ], color=(255, 255, 255), wireframe=True)
        self.pos_x_face = Polygon3D([
            [x+0.3, y, z-0.3],
            [x+0.3, y, z+0.3],
            [x+0.3, y+1.8, z+0.3],
            [x+0.3, y+1.8, z-0.3],
        ], color=(255, 255, 255), wireframe=True)
        self.neg_x_face = Polygon3D([
            [x-0.3, y, z-0.3],
            [x-0.3, y, z+0.3],
            [x-0.3, y+1.8, z+0.3],
            [x-0.3, y+1.8, z-0.3],
        ], color=(255, 255, 255), wireframe=True)
        self.eye_level = Polygon3D([
            [x-0.3, y+1.62, z-0.3],
            [x-0.3, y+1.62, z+0.3],
            [x+0.3, y+1.62, z+0.3],
            [x+0.3, y+1.62, z-0.3],
        ], color=(255, 0, 0), wireframe=True)
        dx = cos(yaw + pi/2)*cos(-pitch)
        dy = sin(-pitch)
        dz = sin(yaw + pi/2)*cos(-pitch)
        self.look_ray = Polygon3D([
            [x, y+1.62, z],
            [x+dx*3, y+dy*3+1.62, z+dz*3]
        ], color=(0, 0, 255), wireframe=True, z_override=-2)
        self.f3_look_ray = Polygon3D([
            [x, y+1.62, z],
            [x+dx*20, y+dy*20+1.62, z+dz*20]
        ], color=(0, 255, 0), wireframe=True, z_override=-1)

    def queue_draw(self):
        self.bottom_face.queue_draw()
        self.top_face.queue_draw()
        self.pos_x_face.queue_draw()
        self.neg_x_face.queue_draw()
        self.eye_level.queue_draw()
        self.look_ray.queue_draw()
        self.f3_look_ray.queue_draw()


def distance(p1: Sequence[float], p2: Sequence[float]):
    return sum((a - b)**2 for a, b in zip(p1, p2))**(1/2)


def main():
    cam_p = [0.0, 0.0, 0.0]
    cam_r = [0.0, 0.0, 0.0]

    player_hitbox = PlayerHitbox([0.11425055107308556, 3.4400000000000013-1.62, 0.19886227225051142], 0.008000000000009777, 0.6540000000000001)

    running = True
    while running:
        skip_mouse_move = False
        screen.fill((116, 179, 255))
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            if event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:
                    pygame.mouse.set_relative_mode(not pygame.mouse.get_relative_mode())
                    skip_mouse_move = True
                if event.button == 3:
                    player_hitbox = PlayerHitbox([cam_p[0], cam_p[1]-1.62, cam_p[2]], cam_r[1], cam_r[0])
                    # print(cam_p, cam_r[1], cam_r[0])

        # do the important stuff
        blocks = []
        for v in range(0, 10):
            blocks.append(Block([v, 0, v], "grass_block", wireframe=False))
        for block in blocks:
            block.queue_draw()

        if player_hitbox is not None:
            player_hitbox.queue_draw()
            yaw, pitch = player_hitbox.yaw, player_hitbox.pitch
            px, py, pz = player_hitbox.x, player_hitbox.y+1.62, player_hitbox.z
            opx, opy, opz = px, py, pz
            dx = cos(yaw + pi/2)*cos(-pitch)
            dy = sin(-pitch)
            dz = sin(yaw + pi/2)*cos(-pitch)
            for i in range(400):
                sx = floor(px)+1 if dx > 0 else ceil(px)-1
                sy = floor(py)+1 if dy > 0 else ceil(py)-1
                sz = floor(pz)+1 if dz > 0 else ceil(pz)-1
                if dx == 0:
                    xt = 99999
                else:
                    xt = (sx - px) / dx
                if dy == 0:
                    yt = 99999
                else:
                    yt = (sy - py) / dy
                if dz == 0:
                    zt = 99999
                else:
                    zt = (sz - pz) / dz
                mintime = min(xt, yt, zt)
                px += dx * mintime
                py += dy * mintime
                pz += dz * mintime
                if distance([px, py, pz], [opx, opy, opz]) >= 20:
                    break
                bfs: list[BlockFace] = []
                if mintime == xt:
                    bfs.append(BlockFace([round(px) - 1, floor(py), floor(pz)], FaceDirection.EAST, wireframe=True))
                if mintime == yt:
                    bfs.append(BlockFace([floor(px), round(py) - 1, floor(pz)], FaceDirection.UP, wireframe=True))
                if mintime == zt:
                    bfs.append(BlockFace([floor(px), floor(py), round(pz) - 1], FaceDirection.SOUTH, wireframe=True))
                face_is_block_face = False
                for bf in bfs:
                    for block in blocks:
                        for bf2 in block.get_faces():
                            if bf == bf2:
                                face_is_block_face = True
                    bf.queue_draw()
                if face_is_block_face:
                    print(i)
                    break


        # draw faces by z order
        faces_to_draw.sort(key=lambda f: f.z_override or distance(f.get_center_pos(), cam_p), reverse=True)
        for face in faces_to_draw:
            face.do_draw_now(cam_p, cam_r)
        faces_to_draw.clear()

        # camera movement stuff
        if pygame.mouse.get_relative_mode():
            mouse_dx, mouse_dy = pygame.mouse.get_rel()
            if not skip_mouse_move:
                cam_r[1] = ((cam_r[1] + mouse_dx * MOUSE_SENS + pi) % (2*pi)) - pi
                cam_r[0] = max(-pi/2, min(pi/2, cam_r[0] + mouse_dy * MOUSE_SENS))
        keys = pygame.key.get_pressed()
        fb = (keys[pygame.K_w] - keys[pygame.K_s]) / FRAMERATE
        lr = (keys[pygame.K_d] - keys[pygame.K_a]) / FRAMERATE
        cam_p[0] -= (fb * sin(cam_r[1]) + lr * cos(cam_r[1])) * FLY_SPEED
        cam_p[1] += (keys[pygame.K_SPACE] - keys[pygame.K_LSHIFT]) / FRAMERATE * FLY_SPEED
        cam_p[2] += (fb * cos(cam_r[1]) - lr * sin(cam_r[1])) * FLY_SPEED

        # crosshair
        pygame.draw.line(screen, (173, 165, 192), [WIDTH/2-6, HEIGHT/2], [WIDTH/2+7, HEIGHT/2], width=2)
        pygame.draw.line(screen, (173, 165, 192), [WIDTH/2, HEIGHT/2-6], [WIDTH/2, HEIGHT/2+7], width=2)

        # text informations
        screen.blit(font.render(f"XYZ: {cam_p[0]:.4f} / {cam_p[1]:.4f} / {cam_p[2]:.4f}", False, (255, 255, 255)), (4, 4))
        yaw_deg = cam_r[1] * 180 / pi
        pitch_deg = cam_r[0] * 180 / pi
        cardinal_dir = "unknown"
        if -45.0 <= yaw_deg <= 44.9:
            cardinal_dir = "south"
        if -135.0 <= yaw_deg <= -45.1:
            cardinal_dir = "east"
        if 45.0 <= yaw_deg <= 134.9:
            cardinal_dir = "west"
        if 135.0 <= yaw_deg or yaw_deg <= -135.1:
            cardinal_dir = "north"
        screen.blit(font.render(f"({cardinal_dir}) {yaw_deg:.4f} / {pitch_deg:.4f}", False, (255, 255, 255)), (4, 4+18))

        pygame.display.flip()
        clock.tick(FRAMERATE)
    pygame.quit()


if __name__ == '__main__':
    main()
