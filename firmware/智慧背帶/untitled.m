clear all;
data = readtable('2020-05-04 07:42:55:001_ACC_4727A261.txt', 'Delimiter', ',');
data2 = readtable('2020-05-04 12:43:39:523_ACC_DC1DB007.txt', 'Delimiter', ',');
data3 = readtable('2020-05-06 11:48:15:191_ACC_F1D29EF3.txt', 'Delimiter', ',');



X_vect1 = data.Var2 * 4;
Y_vect1 = data.Var3 * 4;
Z_vect1 = data.Var4 * 4;

X_vect2 = data2.Var2 * 4;
Y_vect2 = data2.Var3 * 4;
Z_vect2 = data2.Var4 * 4;

X_vect3 = data3.Var2 * 4;
Y_vect3 = data3.Var3 * 4;
Z_vect3 = data3.Var4 * 4;

Z_vect1 = Z_vect1(1:750);
Z_vect2 = Z_vect2(1:750);
Z_vect3 = Z_vect3(1:375);

% t = "";
% for i=1:375
%     t = t + Z_vect3(i) + ",";
% end
% 
% Z_vect1 = [zeros(375, 1); Z_vect1];
% Z_vect2 = [zeros(375, 1); Z_vect2];
% Z_vect3 = [zeros(375, 1); Z_vect3];



% figure(1)
% subplot(311);
% plot(Z_vect3);
% % ylim([-90, -30]);
% title("閉氣")
% ylabel("mg")
% xlabel('point')
% subplot(312);
% plot(Z_vect2);
% title("正常呼吸")
% ylabel("mg")
% xlabel('point')
% subplot(313);
% plot(Z_vect1);
% title("快速呼吸")
% ylabel("mg")
% xlabel('point')




lp = designfilt('lowpassfir', ...
         'FilterOrder',25, ...
         'CutoffFrequency',0.8, ...
         'SampleRate',25);
%  fvtool(lp);
y = filter(lp, Z_vect2);

figure(2)
subplot(211);
plot(Z_vect2);
title("Z axis raw data (25Hz)")
ylabel("mg")
xlabel('point')
subplot(212);
plot(y);
title('after lowpass filter')
ylabel("mg")
xlabel('point')
% 
IndMax = find(diff(sign(diff(y)))<0)+1;
IndMin = find(diff(sign(diff(y)))>0)+1;

hold on
plot(IndMax, y(IndMax(:)), 'o');
% plot(IndMin, y(IndMin(:)), 'x', 'color', 'black');

IndMax = [0; IndMax];
threshold = 3;
addList = [];
addList = [addList IndMax(2)];
for i=2:length(IndMax)
    for j=1:length(IndMin) - 1
        if IndMin(j) < IndMax(i) && IndMin(j) > IndMax(i-1)
            if abs(y(IndMin(j)) - y(IndMax(i))) > threshold ...
                    && abs(y(IndMin(j+1)) - y(IndMax(i))) > threshold
                addList = [addList IndMax(i)];
            end
        end
    end
end
% ans = [];
% for i=2:length(addList)
%     disp(y(addList(i)) - min(y(addList(i-1):addList(i))));
%     if y(addList(i)) - min(y(addList(i-1):addList(i))) > 2
%         ans = [ans addList(i)];
%     end
% end

% subplot(313)
% plot(y);
% title('smooth')
% ylabel("mg")
% xlabel('point')
% hold on
% plot(addList, y(addList(:)), 'o');


