#include "read_file.h"
#include <assert.h>
#include <stdbool.h>
#include <math.h>
const float SMALL_FALL_THRES_HIGH = 4.9f;
const float HIGH_FALL_THRRES_HIGH = 30.0f;
const float DECLINE_BEFORE_FALL_THRES_LOW = -0.5f;
const int WRITE_SIZE = 55;
const int SAMPLE_FREQ = 25;
extern FILE* verbose;
Acc_values create_acc_values()
{
    Acc_values acc;
    acc.is_result_avail = false;
    acc.result = 0;
    acc.xyz_axis.x = 0;
    acc.xyz_axis.y = 0;
    acc.xyz_axis.z = 0;
    return acc;
}

// �n�Q��k�P�_�O�G�ܤj���O�t�ܤj
Chunk* new_chunk(const unsigned char chunk_size)
{
    assert(chunk_size <= MAX_CHUNK_SIZE);
    Chunk* ck = (Chunk*)malloc(sizeof(Chunk));
    ck->size_limit = chunk_size;
    ck->current_size = 0;
    ck->last_index_ref = 0;
    for(int i = 0; i < chunk_size; i++)
    {\
        ck->isValid[i] = false;
    }
    return ck;
}

bool chunk_append(Chunk* chunk, Acc_values acc)
{
    // not enough size
    if(chunk->current_size >= chunk->size_limit)
    {
        return false;
    }
    chunk->accs[chunk->current_size] = acc;
    chunk->isValid[chunk->current_size] = true;
    chunk->current_size++;
    return true;
}
bool chunk_append_withbuffer(Chunk* chunk,float xc, float yc, float zc)
{
    // not enough size
    if(chunk->current_size == chunk->size_limit)
    {
        return false;
    }
    chunk->accs[chunk->current_size].xyz_axis.x = xc;
    chunk->accs[chunk->current_size].xyz_axis.y = yc;
    chunk->accs[chunk->current_size].xyz_axis.z = zc;
    chunk->isValid[chunk->current_size++] = true;
    return true;
};

int num_falls(Chunk* chunk, float thresh_hi, float thresh_lo, int interval, const unsigned char cooldown)
{
    #define NONE 50
    int return_num_falls = 0;
    static int chunk_serial = 0;
    static int peak_left_down = NONE;
    static int peak_right_down = NONE;
    static int two_down_logger = 0;
    static bool threlo = false;
    static unsigned char cd = 0;
    // ���W��chunk�W�X��
    if(peak_left_down != NONE || two_down_logger > 0)
    {
        peak_left_down = -1*(chunk->size_limit-1-peak_left_down);
    }
    for(int frame = 0; frame < chunk->current_size; frame++)
    {
        // >= !!!!
        threlo = chunk->accs[frame].result >= thresh_lo;
        // �F��W�� && �L�h�S��쥪�y�L
        if(threlo && peak_left_down == NONE)
        {
            peak_left_down = frame;
            two_down_logger = 0;
        } // if �W�X�W�� ��& �S�����y
        // �w�g�F��W�u�åB�b�յ۴M��k�y
        else if(threlo && peak_left_down != NONE)
        {
            // �����@�s��X��frame�I��W��
            two_down_logger++;
        }
        // �w�g��쥪�y�ӥB�X�{�k�y
        else if(peak_left_down != NONE && !threlo)
        {
            peak_right_down = frame;
        }
        // ����
        // �w�g��쥪�k�y
        if(peak_left_down != NONE && peak_right_down != NONE)
        {
            // �϶����p
            if(peak_right_down - peak_left_down <= interval && cd <= 0)
            {
                if(peak_left_down >= 0)
                {
                    for(int c = -4; c < 4; c++)
                    {
                        if(peak_left_down+c >= 0 && peak_left_down+c < chunk->current_size)
                        {
                            printf("value %f at index %d\n", chunk->accs[peak_left_down+c].result, (c+peak_left_down) + chunk_serial*chunk->size_limit);
                        }
                    }
                    printf("------\n");

                }
                return_num_falls++;
                cd = cooldown;
            }
            else
            {

            }
            two_down_logger = 0;
            peak_left_down = NONE;
            peak_right_down = NONE;
        }
        if(cd -1 >= 0) cd--;
    } // for �j��
    chunk_serial++;
    return return_num_falls;
}
// unused
XYZAxis find_offset(Chunk* chunk)
{
    float x = 0;
    float y = 0;
    float z = 0;
    // need at least data within 1 sec
    assert(chunk->current_size <= chunk->size_limit);
    for(int i = 0; i < chunk->current_size; i++)
    {
        x += chunk->accs[i].xyz_axis.x;
        y += chunk->accs[i].xyz_axis.y;
        z += chunk->accs[i].xyz_axis.z;
    }
    x /= chunk->current_size;
    y /= chunk->current_size;
    z /= chunk->current_size;
    XYZAxis xyz= {.x = x, .y = y, .z = z};
    return xyz;

}
// unused
// ���� offset ���k�s
void fix_chunk_values_old(Chunk* current, XYZAxis* offsets_xyz)
{
    float* xptr, *yptr, *zptr;
    for(int i = 0; i < current->current_size; i++)
    {
        // set pointers for data because var name is too long
        xptr = &current->accs[i].xyz_axis.x;
        yptr = &current->accs[i].xyz_axis.y;
        zptr = &current->accs[i].xyz_axis.z;
        *xptr -= offsets_xyz->x;
        *yptr -= offsets_xyz->y;
        *zptr -= offsets_xyz->z;
    }
}
void verbose_result(Chunk* unsmoothed, Chunk* smoothed)
{
    float x,y,z;
    int s = 0;
    for(int i = 0; i < unsmoothed->current_size; i++)
    {
        for(Chunk* current = unsmoothed;s<2;current = smoothed,s++)
        {
            // set pointers for data because var name is too long
            x = current->accs[i].xyz_axis.x;
            y = current->accs[i].xyz_axis.y;
            z = current->accs[i].xyz_axis.z;
            current->accs[i].result = (float)sqrt(x*x+y*y+z*z);
            if(current == unsmoothed)
            {
                fprintf(verbose, "%f, ",current->accs[i].result);
            }
            else
            {
                fprintf(verbose, "%f\n",current->accs[i].result);
            }
        }
    }


}
void generate_result(Chunk* current, bool use_verbose)
{
    float x,y,z, res;
    for(int i = 0; i < current->current_size; i++)
    {
        // set pointers for data because var name is too long
        x = current->accs[i].xyz_axis.x;
        y = current->accs[i].xyz_axis.y;
        z = current->accs[i].xyz_axis.z;
        current->accs[i].result = (float)sqrt(x*x+y*y+z*z);
        res = current->accs[i].result;
    }
}
// �� adjacent value smooth algo
void smooth_chunk(Chunk* current, const int smooth_window_size)
{
    float smooth_win = (float)smooth_window_size;
    assert(smooth_window_size >= 0);
    assert(smooth_window_size <= current->current_size);
    float sumX, sumY, sumZ;
    // window �|���X�X�� block
    int smooth_times = current->current_size / smooth_window_size ;
    int last_smooth_times = current->current_size % smooth_window_size;
    int divisor = 0;
    for(int frame = 0; frame < current->current_size; frame+=smooth_window_size)
    {
        divisor = 0;
        sumX = 0;
        sumY = 0;
        sumZ = 0;
        for(int subframe = frame; subframe < frame + smooth_window_size && subframe < current->current_size; subframe++)
        {
            divisor++;
            sumX += current->accs[subframe].xyz_axis.x;
            sumY += current->accs[subframe].xyz_axis.y;
            sumZ += current->accs[subframe].xyz_axis.z;
        }
        sumX /= divisor;
        sumY /= divisor;
        sumZ /= divisor;
        for(int subframe = frame; subframe < frame + smooth_window_size && subframe < current->current_size; subframe++)
        {
            current->accs[subframe].xyz_axis.x = sumX;
            current->accs[subframe].xyz_axis.y = sumY;
            current->accs[subframe].xyz_axis.z = sumZ;
        }
    }
}

void sput_single_acc(Acc_values* const acc, char* cstring)
{
    snprintf(cstring, WRITE_SIZE,"%.8f%.8f%.8f\n",
             acc->xyz_axis.x,
             acc->xyz_axis.y,
             acc->xyz_axis.z);
}
void fput_single_acc(Acc_values* const acc, FILE* const fileptr)
{
    char buf[WRITE_SIZE];
    snprintf(buf, WRITE_SIZE, "%.8f,%.8f,%.8f\n",
             acc->xyz_axis.x, acc->xyz_axis.y, acc->xyz_axis.z);
    fputs(buf,fileptr);
}

void init_cur_status(Current_State* cstate)
{
    cstate->axis_orientation = NORMAL;
    cstate->fall_times = 0;
}

void generate_vector_sum_(Chunk* chunk, XYZAxis* offset)
{
    XYZAxis* xyz = NULL;
    float x,y,z;
    for(int i = 0; i < chunk->current_size; i++)
    {
        xyz = &chunk->accs[i].xyz_axis;
        x = xyz->x;
        x -= offset->x;
        y = xyz->y;
        y -= offset->y;
        z = xyz->z;
        z -= offset->z;
        chunk->accs[i].is_result_avail = true;
        chunk->accs[i].result = x + y + z;
    }
}

float get_std(float results[], size_t size)
{
    float average = 0.0f;
    float diffsum = 0.0f;
    for(int i = 0; i < size; i++)
    {
        average += results[i];
    }
    average /= size;
    for(int i = 0; i < size; i++)
    {
        diffsum += pow(results[i]-average,2);
    }
    diffsum /= size;
    return sqrt(diffsum);
}
size_t get_breath_times(float results[], size_t size, float difference)
{
    int times = 0;
    float prev, mid, next;
    bool firstTime = true;
    bool secondTime = false;
    const float DUMMY = -1.0f;
    int count = 0;
    float peakVal = DUMMY;
    float troughVal = DUMMY;
    int diff = 0;
    float current;
    for(int i = 0; i < size; i++)
    {
        if(i == 0)
        {
            troughVal = results[0];
        }
        else
        {
            if(count == 0)
            {
                count = 1;
                continue;
            }
            if(results[count -1] < results[count])
            {
                if(diff == NEG)
                {
                    troughVal = results[count];
                }
                diff = POS;
            }
            else if(results[count -1] > results[count])
            {
                if(diff == POS)
                {
                    peakVal = results[count];
                }
                diff = NEG;
            }
            else
            {
                diff = ZERO;
            }
            if(peakVal != DUMMY && troughVal != DUMMY)
            {
                if(peakVal - troughVal >= difference)
                {
                    times++;
                }
            }
        }
        count++;
    }
    return times;
}
int breath_status(size_t times, float stdvar, size_t uplim, size_t downlim, float stdlim)
{
    if(stdvar < stdlim)
    {
        puts("little");
        return TOOFEW;
    }
    if(times < downlim)
    {
        puts("down lim");
        return TOOFEW;
    }
    if(times > uplim && stdvar > stdlim) return TOOMANY;
    if(times <= uplim && times >= downlim && stdvar) return OK;
    return ERROR;
}

