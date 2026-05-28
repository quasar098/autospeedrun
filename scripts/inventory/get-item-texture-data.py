from PIL.Image import open as open_img
from os import listdir
from os.path import join, isdir


TEXTURES_ITEM_PATH = r"D:\Downloads\stuff\assets\minecraft\textures\item"

if not isdir(TEXTURES_ITEM_PATH):
    print("replace TEXTURES_ITEM_PATH with minecraft/textures/item directory of mc assets")
    exit(1)

i = 0
for fname in listdir(TEXTURES_ITEM_PATH):
    path = join(TEXTURES_ITEM_PATH, fname)
    img = open_img(path).convert("RGBA")
    if img.size != (16, 16):
        continue
    data = [int(bytes(img.getpixel((x, y))[::-1]).hex(), 16) for y in range(img.height) for x in range(img.width)]
    data = [(v if v & 0xff000000 else 0) for v in data]
    squot = "'"
    if i % 30 == 0:
        print(f"    }}\n\n    private static void loadImages{i//30}() {{")
    print(f"        images.put(\"{fname.removesuffix('.png')}\", new int[] {str([hex(v) for v in data]).replace('[', '{ ').replace(']', ' }').replace(squot, '')});")
    i += 1
