a = load('¥ª­Ë.mat').unnamed;
b = load('¥k­Ë.mat').unnamed2;
c = load('¥¿­Ë.mat').unnamed3;
d = load('«á­Ë.mat').unnamed1;
e = load('¶^¸¨.mat').unnamed4;

a_vm = sqrt(a(:, 1).^2 + a(:, 2).^2 + a(:, 3).^2);
b_vm = sqrt(b(:, 1).^2 + b(:, 2).^2 + b(:, 3).^2);
c_vm = sqrt(c(:, 1).^2 + c(:, 2).^2 + c(:, 3).^2);
d_vm = sqrt(d(:, 1).^2 + d(:, 2).^2 + d(:, 3).^2);
e_vm = sqrt(e(:, 1).^2 + e(:, 2).^2 + e(:, 3).^2);


subplot(511)
plot(a_vm)
line([0,length(a_vm)],[2,2], 'color', 'r')
title('¥ª­Ë')
ylim([0, 3])
xlabel('point')
ylabel('g')
subplot(512)
plot(b_vm)
line([0,length(b_vm)],[2,2], 'color', 'r')
title('¥k­Ë')
ylim([0, 3])
xlabel('point')
ylabel('g')
subplot(513)
plot(c_vm)
line([0,length(c_vm)],[2,2], 'color', 'r')
title('¥¿­Ë')
ylim([0, 3])
xlabel('point')
ylabel('g')
subplot(514)
plot(d_vm)
line([0,length(d_vm)],[2,2], 'color', 'r')
title('«á­Ë')
ylim([0, 3])
xlabel('point')
ylabel('g')
subplot(515)
plot(e_vm)
line([0,length(e_vm)],[2,2], 'color', 'r')
title('¶^¸¨')
ylim([0, 3])
xlabel('point')
ylabel('g')