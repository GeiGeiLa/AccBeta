#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "headers.h"
#include "read_file.h"
#define BUF_SIZE 55
int count = 0;

Acc_values create_Acc_obj_fromstr(char* const linestr);
Acc_values acc_temp;
const int BUFFER_SIZE = BUF_SIZE;
Current_State current_state;
char buf[BUF_SIZE];
FILE* input, *output, *verbose;
const float HIGH_THRES = 2.0f;
const int MAX_INTERVAL = 7;
const int SMOOTH_SIZE = 2;
unsigned char cooldown = 5;
int booms = 0;
int main()
{
    int i;
    printf("1:left 2:front 3:right 4:back\n");
    fscanf(stdin,"%d",&i);
    input;
    if(i == 0) input = fopen("left.csv","r");
    else if(i == 1) input = fopen("front.csv","r");
    else if(i==2) input = fopen("right.csv","r");
    else if(i == 3) input = fopen("back.csv","r");
    else exit(EXIT_FAILURE);
    init_cur_status(&current_state);
    puts("hi");
    output = fopen("outfall.txt","w");
    verbose = fopen("verbose.txt","w");
    if(input == NULL)
    {
        printf("Error while reading file\n");
        system("pause");
        return -1;
    }
    puts("open success");
    i = 0;
    bool chunk_succeed = false;
    Chunk* chunkptr = new_chunk(SAMPLE_FREQ);
    bool pivot_found = false;
    XYZAxis offset;
    while(fgets(buf, BUFFER_SIZE, input))
    {
        // prevent buffer overflow
        buf[BUF_SIZE-1]='\0';
        acc_temp = create_Acc_obj_fromstr(buf);

        chunk_succeed = chunk_append(chunkptr,acc_temp);
        // failed because chunk full

        if(!chunk_succeed)
        {
            smooth_chunk(chunkptr, 2);
            generate_result(chunkptr,false);
            int falls = num_falls(chunkptr, -1, HIGH_THRES, MAX_INTERVAL,5);
            booms += falls;
            free(chunkptr);
            chunkptr = new_chunk(SAMPLE_FREQ);
            chunk_append(chunkptr,acc_temp);
        }
    }
    free(chunkptr);
    fclose(input);
    fclose(output);
    printf("counts:%d booms:%d\n",count,booms);
    return 0;
}
Acc_values create_Acc_obj_fromstr(char* const str)
{
    count++;
    Acc_values acc;
    //float x,y,z;
    // prevent buffer overflow
    str[BUFFER_SIZE-1] = '\0';
    char* token;
    char delimiters[]=",";
    float values[3];
    int i;
    for(i=0, token = strtok(str,delimiters); token != NULL; token = strtok(NULL, delimiters),i++)
    {
        if(i==0)continue;
        sscanf(token,"%f",&values[i-1]);
    }
    acc.xyz_axis.x = values[0];
    acc.xyz_axis.y = values[1];
    acc.xyz_axis.z = values[2];
    //printf("%.8f__%.8f__%.8f__\n",acc.xyz_axis.x, acc.xyz_axis.y, acc.xyz_axis.z);
    return acc;

}
// unused
void strtok_example(char* str, char* delimiters)
{
    if(str == NULL || strlen(str) == 0 || delimiters == NULL)
    {
        puts("str dummy");
        return;
    }
    char* token;
    for(token = strtok(str,delimiters); token != NULL; token = strtok(NULL, delimiters))
    {
        puts(token);
    }
}
