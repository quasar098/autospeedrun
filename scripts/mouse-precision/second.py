import pygame
from random import random
from math import sqrt, floor, ceil, exp

pygame.init()

WIDTH, HEIGHT, FRAMERATE = 1280, 720, 75
CX, CY = WIDTH / 2, HEIGHT / 2
screen = pygame.display.set_mode([WIDTH, HEIGHT])
pygame.display.set_caption("Pixel stepping binary search demonstration")
pygame.display.set_icon(pygame.image.load("./pig.png").convert())
clock = pygame.time.Clock()

font = pygame.font.Font("./JetBrainsMono-Regular.ttf", 24)
rendered_texts = {}

color_a = 35, 206, 107
color_b = 168, 70, 160
color_b_2 = 218, 65, 103
color_c = 68, 157, 209
color_accept = 105, 220, 158
color_reject = 214, 34, 70
bar_height = 40
bar_thick = 3
bar_gap = 15


def get_text(text: str, color: tuple[int, int, int]):
    if text not in rendered_texts:
        rendered_texts[(text, color)] = font.render(text, True, color)
    return rendered_texts[(text, color)]


def render_text(text: str, color=(247, 247, 255), **kwargs):
    text_surf = get_text(text, color)
    screen.blit(text_surf, text_surf.get_rect(**kwargs))


def main():
    # === important stuff ===

    options_txt_sens = 0.40140846371650696
    minecraft_mouse_sens = options_txt_sens * 200
    deg_per_pix = ((options_txt_sens * 0.6 + 0.2)**3 * 8 * 0.15)

    original_real_angle = random() * 360 - 180

    print(f"{minecraft_mouse_sens = }")
    print(f"{deg_per_pix = }")
    print(f"{original_real_angle = }")

    assert len(str(deg_per_pix % 0.1)) > 6, "your sensitivity is lacking specificity (make it have a bunch of decimals)"

    left_accept = 0
    right_accept = 1

    view_pix_offset = 0
    view_angle = original_real_angle
    view_angle_desired = original_real_angle
    view_zoom = 9  # logarithmic

    # === end of important stuff ===

    running = True
    while running:
        scale = 2**view_zoom
        if pygame.mouse.get_pressed(3)[0]:
            original_real_angle = view_angle - (CX - pygame.mouse.get_pos()[0]) / scale
        real_angle = view_pix_offset * deg_per_pix + original_real_angle
        screen.fill((39, 45, 45))
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            if event.type == pygame.MOUSEWHEEL:
                if pygame.key.get_mods() & pygame.KMOD_SHIFT:
                    view_zoom += event.precise_y/4
                else:
                    view_angle_desired += event.precise_y * 30 / scale
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_RIGHT:
                    view_pix_offset += 1
                elif event.key == pygame.K_LEFT:
                    view_pix_offset -= 1
                elif event.key == pygame.K_SPACE:
                    # do a step of binary search
                    best_i = None
                    mid = (right_accept + left_accept) / 2
                    for i in range(-300, 300):
                        if best_i is None:
                            best_i = i
                            continue
                        angle_offset = (i * deg_per_pix) % 0.1- mid/10
                        best_angle_offset = (best_i * deg_per_pix) % 0.1 - mid/10
                        if abs(angle_offset) <= abs(best_angle_offset):
                            best_i = i
                    print('pix to move from orig:', best_i)
                    view_pix_offset = best_i
                    real_angle = view_pix_offset * deg_per_pix + original_real_angle
                    lower = round(round(original_real_angle, 1)-0.05, 2)
                    upper = round(round(original_real_angle, 1)-0.05, 2)+0.1
                    print(lower + deg_per_pix * view_pix_offset, upper + deg_per_pix * view_pix_offset)
                    # TODO
                    # if round(real_angle, 1) >= round(lower + deg_per_pix * view_pix_offset, 1):
                    #     left_accept = lower + deg_per_pix * view_pix_offset
                    # else:
                    #     right_accept = upper + deg_per_pix * view_pix_offset

        # code here
        smoothing_factor = 1.0 - exp(-100.0 / FRAMERATE)
        view_angle = view_angle_desired * (1-smoothing_factor) + smoothing_factor * view_angle

        # top bar (degree -0.5,+0.5 separators)
        render_text(f"original yaw (F3): {original_real_angle:.1f}", color=color_b_2, midtop=(CX, 5))
        render_text(f"current yaw (F3): {real_angle:.1f}", color=color_b, midtop=(CX, 55))
        pygame.draw.line(screen, color_a, (0, CY - bar_gap - bar_height / 2),
                         (WIDTH, CY - bar_gap - bar_height / 2), bar_thick)
        start_i = floor((-CX / scale + view_angle - (round(10 * real_angle) / 10 - 0.5)) * 10)
        for i in range(start_i, start_i+300):
            top_bar_angle = round(10*real_angle)/10-0.5+i/10
            top_bar_x = (top_bar_angle-view_angle)*scale
            if top_bar_x > CX + 24:
                break
            if top_bar_x < -CX - 24:
                continue
            pygame.draw.line(screen, color_a, (CX + top_bar_x, CY - bar_height / 2 - bar_gap),
                             (CX + top_bar_x, CY - bar_height * 3 / 2 - bar_gap), bar_thick)
            top_bar_0p05_x = (top_bar_angle-view_angle+0.05)*scale
            pygame.draw.line(screen, color_a, (CX + top_bar_0p05_x, CY - bar_height / 2 - bar_gap),
                             (CX + top_bar_0p05_x, CY - bar_height / 2 - 5), bar_thick)
            top_text = f"{top_bar_angle:.01f}"
            angle_text = get_text(top_text, color_a)
            rotated_text = pygame.transform.rotate(angle_text, 90)
            screen.blit(
                rotated_text,
                rotated_text.get_rect(midbottom=(CX + top_bar_x, CY - bar_height * 3 / 2 - bar_gap * 2))
            )

        # middle bar (real angle)
        render_text(f"original yaw (real): {original_real_angle}", color=color_b_2, midtop=(CX, 30))
        render_text(f"current yaw (real): {real_angle}", color=color_b, midtop=(CX, 80))
        pygame.draw.line(screen, color_b, (0, CY), (WIDTH, CY), bar_thick)
        original_real_bar_x = (original_real_angle - view_angle) * scale
        pygame.draw.line(screen, color_b_2, (CX + original_real_bar_x, CY - bar_height * 0.2),
                         (CX + original_real_bar_x, CY + bar_height * 0.5), bar_thick)
        real_bar_x = (real_angle - view_angle) * scale
        pygame.draw.line(screen, color_b, (CX + real_bar_x, CY - bar_height / 2),
                         (CX + real_bar_x, CY + bar_height / 2), bar_thick)

        # bottom bar (deg per pix separators)
        pygame.draw.line(screen, color_c, (0, CY + bar_gap + bar_height / 2),
                         (WIDTH, CY + bar_gap + bar_height / 2), bar_thick)
        start_i = floor((-CX / scale + view_angle - real_angle) / deg_per_pix)
        for i in range(start_i, start_i+300):
            real_bar_x = (real_angle - view_angle + i*deg_per_pix) * scale
            if real_bar_x > CX + bar_thick:
                break
            pygame.draw.line(screen, color_c, (CX + real_bar_x, CY + bar_height / 2 + 5),
                             (CX + real_bar_x, CY + bar_height + bar_gap), bar_thick)

        # bottomest bar (range of acceptance)
        pygame.draw.line(screen, color_reject, (10, HEIGHT-15), (WIDTH-10, HEIGHT-15), width=10)
        pygame.draw.line(screen, color_accept, (10+(WIDTH-20)*left_accept, HEIGHT-15),
                         (10+(WIDTH-20)*right_accept, HEIGHT-15), width=10)
        leftangle_text = get_text(f"{round(original_real_angle, 1)-0.05:.2f}", color_accept)
        rightangle_text = get_text(f"{round(original_real_angle, 1)+0.05:.2f}", color_accept)
        rotated_leftangle_text = pygame.transform.rotate(leftangle_text, 90)
        rotated_rightangle_text = pygame.transform.rotate(rightangle_text, 90)
        screen.blit(
            rotated_leftangle_text,
            rotated_leftangle_text.get_rect(bottomleft=(10, HEIGHT - 28))
        )
        screen.blit(
            rotated_rightangle_text,
            rotated_rightangle_text.get_rect(bottomright=(WIDTH - 10, HEIGHT - 28))
        )
        pygame.display.flip()
        clock.tick(FRAMERATE)
    pygame.quit()


if __name__ == '__main__':
    main()
