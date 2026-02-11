from math import sin, cos, pi, atan2, lcm
from itertools import product

# idea: use pulse width modulation in minecraft movement :troll:

# reference: https://www.mcpk.wiki/wiki/Horizontal_Movement_Formulas


PRINT_EVERY_N_TICKS = None


def desmos2d(*a, equals_v = None):
    if len(a) == 1:
        x, z = a[0]
    else:
        x, z = a
    equals = ""
    if equals_v is not None:
        equals = r"v_{" + str(equals_v) + r"}="
    print(fr"{equals}\left({x:.10f},{-z:.10f}\right)")


def gen_pwm(x_rep, z_rep):
    angles = []
    for i in range(132):
        dx = x_rep[i % len(x_rep)]
        dz = z_rep[i % len(z_rep)]
        angles.append(atan2(-dz, dx))
    return angles

# graph right = -90deg = -pi/2 (player left)
# graph up = 0deg = 0 (player forward)
# graph left = 90deg = pi/2 (player right)
# graph down = 180deg = pi (player backward)
# idk if these are even correct
# this is def not the last time mc angles will confuse me

def main():
    x_z_reps_discovered = []

    for dxl in range(1, 5):
        for dzl in range(1, 5):
            for x_rep in product(*dxl*[[-1, 0, 1]]):
                for z_rep in product(*dzl*[[-1, 0, 1]]):
                    bad = False
                    for i in range(lcm(len(x_rep), len(z_rep))):
                        x_rep_v = x_rep[i % len(x_rep)]
                        z_rep_v = z_rep[i % len(z_rep)]
                        if x_rep_v == 0 == z_rep_v:
                            bad = True
                            break
                    if bad:
                        continue
                    if x_rep[0] == 0 and {*x_rep} != {0}:
                        continue
                    if z_rep[0] == 0 and {*z_rep} != {0}:
                        continue
                    res = compute_angle_and_ticks_from_x_z_reps(x_rep, z_rep)
                    if res is None:
                        continue
                    theta, ticks = res
                    closest_vals = None
                    for vals in x_z_reps_discovered:
                        if closest_vals is None or abs(vals[0]-theta) < abs(closest_vals[0]-theta):
                            closest_vals = vals
                    if closest_vals is None:
                        x_z_reps_discovered.append((theta, ticks, x_rep, z_rep))
                    elif abs(theta-closest_vals[0]) > 0.1:
                        x_z_reps_discovered.append((theta, ticks, x_rep, z_rep))
                    elif ticks < closest_vals[1]:
                        x_z_reps_discovered.remove(closest_vals)
                        x_z_reps_discovered.append((theta, ticks, x_rep, z_rep))
    x_z_reps_discovered = sorted(x_z_reps_discovered)
    # for theta, ticks, x_rep, z_rep in x_z_reps_discovered:
    #     global PRINT_EVERY_N_TICKS
    #     PRINT_EVERY_N_TICKS = 6
    #     compute_angle_and_ticks_from_x_z_reps(x_rep, z_rep)
    for theta, ticks, x_rep, z_rep in x_z_reps_discovered:
        row_l = [int(theta * 180 / pi * 100)]
        for i in range(lcm(len(x_rep), len(z_rep))):
            x_rep_v = x_rep[i % len(x_rep)]
            z_rep_v = z_rep[i % len(z_rep)]
            row_l.append(x_rep_v * 10 + z_rep_v + 11)
        print(str(row_l).replace("[","{").replace("]","}") + ",")


def compute_angle_and_ticks_from_x_z_reps(x_rep, z_rep) -> tuple[float, int]:
    v: list[tuple[float, float]] = [(0, 0)]
    p: list[tuple[float, float]] = [(0, 0)]

    slippery_multiplier = 0.6  # default = 0.6

    effects_multiplier = 1.0

    directions = gen_pwm(x_rep, z_rep)

    i = 0
    while True:
        if i >= len(directions):
            # time's up
            return None
        direction = directions[i] + pi
        assert len({x_rep[i % len(x_rep)], z_rep[i % len(z_rep)], 1, -1, 0}) == 3

        movement_state = 1.0  # walking

        if x_rep[i % len(x_rep)] == 1:
            movement_state = 1.3  # sprinting

        if x_rep[i % len(x_rep)] == z_rep[i % len(z_rep)] == 0:
            movement_state = 0.0  # stopping

        movement_multiplier = movement_state * 0.98

        prev_v_x, prev_v_z = v[-1]
        prev_p_x, prev_p_z = p[-1]
        momentum_x = prev_v_x * slippery_multiplier * 0.91
        momentum_z = prev_v_z * slippery_multiplier * 0.91
        accel_x = 0.1 * movement_multiplier * effects_multiplier * (0.6/slippery_multiplier)**3 * sin(direction)
        accel_z = 0.1 * movement_multiplier * effects_multiplier * (0.6/slippery_multiplier)**3 * cos(direction)
        v.append((momentum_x + accel_x, momentum_z + accel_z))
        p.append((prev_p_x + momentum_x + accel_x, prev_p_z + momentum_z + accel_z))
        if PRINT_EVERY_N_TICKS and i % PRINT_EVERY_N_TICKS == 0:
            desmos2d(p[-1], equals_v=i)
        i += 1
        if p[-1][0]**2+p[-1][1]**2 > 25**2:  # 25 blocks distance
            break

    p_sample_1_x, p_sample_1_z = p[len(p)//2]
    p_sample_2_x, p_sample_2_z = p[-1]
    angle = atan2(p_sample_2_z - p_sample_1_z, p_sample_2_x - p_sample_1_x)
    # print(fr"\theta_1={angle:.5f}")
    return angle, i


if __name__ == '__main__':
    main()
