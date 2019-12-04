import java.util.ArrayList;

public class Conf {
    static int c_N;// the number of customers
    static int q_N;// the number of recharge station
    static double dis_m[][] = new double[q_N+c_N][q_N+c_N];// cost_m
    static double Q;// Vehicle fuel tank capacity
    static double C;// Vehicle load capacity
    static double r;// fuel consumption rate
    static double g;// inverse refueling rate
    static double v;// average Velocity
    static void input(String file_name)//
    {

    }
}
class Customers // the Customers
{
    double x;
    double y;
    double r_time;
    double s_time;
    double d_time;
}
class Route
{
    ArrayList<Integer> c_list = new ArrayList<>();// the customers list of vehicle
    boolean check()//check the feasible of the route
    {
        return true;
    }

}
class Solution
{
    ArrayList<Route> r_list = new ArrayList<>();

    boolean check()
    {
        for (Route r:r_list)
        {
            if(!r.check())
                return false;
        }
        return true;
    }

}
