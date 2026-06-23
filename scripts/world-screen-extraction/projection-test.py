from PIL import Image, ImageDraw
from math import tan, sin, cos, pi, atan


img = Image.open("img.png").convert("RGB")
WIDTH = img.width
HEIGHT = img.height
VFOV = 110 * pi/180
HFOV = 2*atan(tan(VFOV/2)*WIDTH/HEIGHT)
print(VFOV, HFOV)


def project(a, c, t):
    ez = 1/tan(HFOV/2)
    t = [pi/180 * t[0], pi/180 * t[1], pi/180 * t[2]]
    sinx, siny, sinz = sin(t[0]), sin(t[1]), sin(t[2])
    cosx, cosy, cosz = cos(t[0]), cos(t[1]), cos(t[2])
    x, y, z = c[0] - a[0], a[1] - c[1], a[2] - c[2]
    dx = cosy*(sinz*y + cosz*x) - siny*z
    dy = sinx*(cosy*z + siny*(sinz*y + cosz*x)) + cosx*(cosz*y - sinz*x)
    dz = cosx*(cosy*z + siny*(sinz*y + cosz*x)) - sinx*(cosz*y - sinz*x)
    if dz <= 0:
        return None
    return (ez/dz*dx) * WIDTH/2 + WIDTH/2, HEIGHT/2 - (ez/dz*dy) * WIDTH/2



c_pos = [-11.890, 66.00000+1.62, -0.811]
c_rot = [36.4, 15.3, 0]

B = [-13, 64, 2]

from_p = project([B[0], B[1]+1, B[2]], c_pos, c_rot)
to_p = project([B[0]+1, B[1]+1, B[2]], c_pos, c_rot)
print(from_p, to_p)

draw = ImageDraw.Draw(img)
draw.line((*from_p, *to_p), fill="red", width=2)
img.save(f"out_img.png")
