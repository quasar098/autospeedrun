from PIL.Image import open as open_img, new as new_img
import hashlib

# wtf ...
# Screen.java
# this.fillGradient(poseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
# ^^^^^^^^^^^^^^^^^ the minecraft pause screen is a gradient and not a solid color

# 0xc0101010 to 0xd0101010

base = 0xffffff
# start = 0xc0101010  # -1072689136
# end = 0xd0101010  # -804253680

img1 = open_img("gradient-slice.png")
img2 = new_img("RGBA", img1.size)
pt = [0, 0, 0]
for y in range(img2.height):
    total = [0, 0, 0]
    for x in range(img2.width):
        nc = tuple(hashlib.sha256(bytes(img1.getpixel((x, y)))).digest()[:3])
        img2.putpixel((x, y), nc)
        total[0] += nc[0]/img2.width
        total[1] += nc[1]/img2.width
        total[2] += nc[2]/img2.width

    if max(abs(a-b) for a,b in zip(pt, total)) > 10:
        img2.putpixel((0, max(y, 0)), (255, 0, 0))
        print(y, end=", ")
    pt = total
img2.save("gradient-slice-out.png")


H = 480
cur = None
for y in range(H):
    a = (0x10 * (y/H) + 0xc0)/255
    cr = round(a * 0x10 + ((base >> 16) & 0xff) * (1-a))
    cg = round(a * 0x10 + ((base >> 8) & 0xff) * (1-a))
    cb = round(a * 0x10 + ((base >> 0) & 0xff) * (1-a))
    c = cr << 16 | cg << 8 | cb
    # print(y, hex(c))
    if hex(c) != cur:
        if cur is not None:
            print('...')
            print(y-1, cur)
        print(y, hex(c))
        cur = hex(c)
