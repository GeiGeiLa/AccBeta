#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include "headers.h"
#include "read_file.h"


Acc_values create_Acc_obj_fromstr(char* const str, bool xyzAll);
Acc_values acc_temp;
Current_State current_state;
char buf[BUF_SIZE];
FILE* input, *output, *verbose;
float HIGH_THRES = 2.0f;
int MAX_INTERVAL = 7;
int SMOOTH_SIZE = 2;
unsigned char cooldown = 5;
int booms = 0;
int countlines(FILE* fp)
{
  // count the number of lines in the file called filename
  int ch=0;
  int lines=0;

  if (fp == NULL)
    return -1;

  lines++;
  while ((ch = fgetc(fp)) != EOF)
    {
      if (ch == '\n')
    lines++;
    }
  rewind(fp);
  return lines;
}
int main()
{
    int BUFFER_SIZE = BUF_SIZE;
    int total = 0;
    int count = 0;
    int i;
    printf("1:left 2:front 3:right 4:back\n");
    fscanf(stdin,"%d",&i);
    if(i==0) input = fopen ("smoothed.csv","r");
    else if(i == 1) input = fopen("left.csv","r");
    else if(i == 2) input = fopen("front.csv","r");
    else if(i==3) input = fopen("right.csv","r");
    else if(i == 4) input = fopen("back.csv","r");
    else exit(EXIT_FAILURE);
    if(input == NULL)
    {
        printf("Error while reading file\n");
        system("pause");
        return -1;
    }
    int breathsecond = 15;
    int BREATH_FRAMES = 125;
    printf("lines%d\n", BREATH_FRAMES);
    i = 0;
    bool chunk_succeed = false;
    Chunk* chunkptr = new_chunk(SAMPLE_FREQ);
    int breath = 0;
    float results_for_breath[BREATH_FRAMES];
    for(int i = 0; i < BREATH_FRAMES; i++)
    {
        results_for_breath[i] = 0.0f;
    }
    size_t breath_times;
    float std;
    int stat;
    while(fgets(buf, BUFFER_SIZE, input))
    {
        total++;
        // prevent buffer overflow
        buf[BUF_SIZE-1]='\0';
        //chunk_succeed = chunk_append(chunkptr,acc_temp);
        // failed because chunk full
        results_for_breath[count++] = create_Acc_obj_fromstr(buf,false).xyz_axis.z;
        if(count >= BREATH_FRAMES)
        {
            breath_times = get_breath_times(results_for_breath, BREATH_FRAMES, 3.0f);
            std = get_std(results_for_breath, BREATH_FRAMES);
            printf("std:%f\n",std);
            stat = breath_status(breath_times, std, 25/4, 8/2,3);
//            switch(stat)
//            {
//
//            case TOOMANY:
//                puts("HUHUHU"); break;
//            case TOOFEW:
//                puts("GRRRRR"); break;
//            }
            count = 0;
        }
//        if(!chunk_succeed)
//        {
//            int falls = num_falls(chunkptr, -1, HIGH_THRES, MAX_INTERVAL,5);
//            booms += falls;
//
//            free(chunkptr);
//
//            chunkptr = new_chunk(SAMPLE_FREQ);
//
//            chunk_append(chunkptr,acc_temp);
//        }
    }
    free(chunkptr);
    fclose(input);
    fclose(output);
    printf("counts:%d booms:%d\n",total,booms);
    return 0;
}
Acc_values create_Acc_obj_fromstr(char* const str, bool xyzAll)
{
    Acc_values acc;
    //float x,y,z;
    // prevent buffer overflow
    if(str == NULL) puts("NULL");
    str[BUF_SIZE-1] = '\0';
    char* token;
    char delimiters[]=",";
    float values[3];
    int i;
    if(xyzAll)
    {
        for(i=0, token = strtok(str,delimiters); token != NULL; token = strtok(NULL, delimiters),i++)
        {
            if(i==0)continue;
            sscanf(token,"%f",&values[i-1]);
        }
    }
    else
    {
        sscanf(str, "%f\n", &values[2]);
    }

    acc.xyz_axis.x = values[0];
    acc.xyz_axis.y = values[1];
    acc.xyz_axis.z = values[2];
    acc.result = sqrt(values[0]*values[0]+values[1]*values[1]+values[2]*values[2]);
    //printf("%.8f__%.8f__%.8f__\n",acc.xyz_axis.x, acc.xyz_axis.y, acc.xyz_axis.z);
    return acc;

}
// unused

