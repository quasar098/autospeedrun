from PIL.Image import open as open2

font_text = open2("ascii.png", 'r')

with open("glyph_sizes.bin", 'rb') as f:
    sizes = f.read()

check_map = [0] * (127 - 32)
letters = '_abcdefghijklmnopqrstuvwxyz:0123456789# '
# 63, 65, 66, 67, 68, 69, 71, 72, 74, 77, 78, 79, 80, 81, 82, 83, 85, 86, 87, 88, 89, 90, 70, 75, 84, 76, 26, 73
widths = {}
for c in range(32, 127):
    b = sizes[c]
    l, r = b >> 4 & 15, (b & 15) + 1
    x = (c % 16) * 8
    y = (c // 16) * 8
    w = 1
    for dx in range(8):
        if not any(font_text.getpixel((x+dx, y+dy))[3] for dy in range(8)):
            break
        w += 1
    if c == ord('"'):
        w = 4
    widths[chr(c)] = w
print([ord(c)-32 for c in sorted(letters, key=widths.get, reverse=True)])
