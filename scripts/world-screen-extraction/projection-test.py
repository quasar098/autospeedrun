from PIL import Image, ImageDraw
from math import tan, sin, cos, pi, atan


img = Image.open("img_2.png").convert("RGB")
WIDTH = img.width
HEIGHT = img.height
VFOV = 110 * pi/180
HFOV = 2*atan(tan(VFOV/2)*WIDTH/HEIGHT)


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


def main():
    # [x, y+1.62, z]
    # c_pos = [-11.890, 66.00000+1.62, -0.811]
    c_pos = [-13.456, 64.00000+1.62, 6.871]
    # [pitch, yaw, 0]
    # c_rot = [36.4, 15.3, 0]
    c_rot = [3.5, -156.0, 0]

    # B = [-13, 64, 2]
    B = [-12, 65, 3]

    # draw = ImageDraw.Draw(img)
    # dl = lambda from_3d, to_3d: draw.line((*project(from_3d, c_pos, c_rot), *project(to_3d, c_pos, c_rot)), fill="red", width=2)
    # dl([B[0], B[1]+1, B[2]], [B[0]+1, B[1]+1, B[2]])
    # dl([B[0]+1, B[1]+1, B[2]], [B[0]+1, B[1]+1, B[2]+1])
    # dl([B[0]+1, B[1]+1, B[2]+1], [B[0], B[1]+1, B[2]+1])
    # dl([B[0], B[1]+1, B[2]+1], [B[0], B[1]+1, B[2]])

    blockface = Image.new("RGB", (16, 16))
    for x in range(16):
        for y in range(16):
            # top face
            # xf, yf = project([B[0]+(1+x*2)/32, B[1]+1, B[2]+(1+y*2)/32], c_pos, c_rot)
            # west (neg x)
            xf, yf = project([B[0], B[1]+1-(1+y*2)/32, B[2]+(1+x*2)/32], c_pos, c_rot)
            blockface.putpixel((x, y), img.getpixel((round(xf), round(yf))))

    img.save(f"out_img.png")
    blockface.save(f"out_blockface.png")


if __name__ == '__main__':
    main()
