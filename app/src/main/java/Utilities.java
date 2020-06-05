import java.security.InvalidParameterException;

public class Utilities {
    public static boolean StringNullOrEmpty(String str)
    {
        if(str == null) return true;
        if(str.length() == 0) return true;
        return false;
    }
    public class KDateTime
    {
        private short year, month, day, hour, minute, second;

        /**
         * constructor accepts all ints or all shorts but not mixed
         */
        public KDateTime(int y, int m, int d, int h, int mm, int s)
        {
            if( y < 0 || m <= 0 || d <= 0 || h < 0 || mm < 0 || s < 0)
            {
                throw new InvalidParameterException("Invalid datetime format detected.");
            }
            year = (short)y; month = (short)m; day = (short)d; hour = (short)h; minute = (short)mm; second = (short)s;
        }
        /**
         * constructor accepts all ints or all shorts but not mixed
         */
        public KDateTime(short y, short m, short d, short h, short mm, short s)
        {
            if( y < 0 || m <= 0 || d <= 0 || h < 0 || mm < 0 || s < 0)
            {
                throw new InvalidParameterException("Invalid datetime format detected.");
            }
            year = y; month = m; day = d; hour = h; minute = mm; second = s;
        }

        /**
         *
         * @return datetime in this form: 2020-03-11 14:21:32
         */
        @Override
        public String toString()
        {
            return year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second;
        }
        /**
         *
         * @return Date in this form: 2020-03-11
         */
        public String GetDate()
        {
            return year+"-"+month+"-"+day;
        }
        public String GetTime()
        {
            return hour+":"+minute+":"+second;
        }
        public String GetTaiwaneseDate(boolean withMingGuo)
        {
            String s = withMingGuo? "民國":"";
            return s;
        }

    }
}
