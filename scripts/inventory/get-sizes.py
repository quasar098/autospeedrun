from PIL.Image import open as open_img
from os import listdir
from os.path import join


for fname in listdir("images"):
    img = open_img(join("images", fname), 'r').convert("RGBA")
    assert img.getpixel((0, 0))[3] == img.getpixel((1, 0))[3] == img.getpixel((0, 1))[3] == 0, fname
    x = 2
    while img.getpixel((x, 3)) != (0, 0, 0, 255):
        x += 1
    x += 1
    y = 2
    while img.getpixel((3, y)) != (0, 0, 0, 255):
        y += 1
    y += 1
    print(fname, x, y)
