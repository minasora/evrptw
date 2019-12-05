import java.awt.desktop.SystemSleepEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Conf {
    static int c_N=100;// the number of customers
    static int q_N=25;// the number of recharge station
    static double dis_m[][];// cost_m
    static double Q;// Vehicle fuel tank capacity
    static double C;// Vehicle load capacity
    static double r;// fuel consumption rate
    static double g;// inverse refueling rate
    static double v;// average Velocity
    static Customer customers[] = new Customer[c_N+1];
    static Customer chargestations[] = new Customer[q_N];
    static Q_best q_bests[];
    static void input(String file_name) throws FileNotFoundException
    {
        String ID;
        String Type;
        double x;
        double y;
        double demand;
        double r_time;
        double d_time;
        double s_time;

        File file = new File("evrptw_instances/"+file_name);
        Scanner cin = new Scanner(file);
        cin.nextLine();
        int i=0;
        int j=0;
        while(cin.hasNext()) {
            ID = cin.next();
            if(ID.equals("END"))
                break;
            Type = cin.next();
            x = cin.nextDouble();
            y = cin.nextDouble();
            demand = cin.nextDouble();
            r_time = cin.nextDouble();
            d_time = cin.nextDouble();
            s_time = cin.nextDouble();
            if (Type.equals("f")) {
                chargestations[j] = new Customer(ID, Type, x, y, r_time, s_time, d_time, demand);
                j++;
            } else {
                customers[i] = new Customer(ID, Type, x, y, r_time, s_time, d_time, demand);
                i++;
            }
        }
        Q = cin.nextDouble();
        C = cin.nextDouble();
        r = cin.nextDouble();
        g = cin.nextDouble();
        v = cin.nextDouble();
        q_N = j;// update the numbers of customers and q;
        c_N = i;
    }
    static void initialize()//get the q_best and the cost_m
    {
        dis_m = new double[c_N][c_N];
        for(int i=0;i<c_N;i++)
        {
            for(int j=0;j<c_N;j++)
            {
                dis_m[i][j] = customers[i].get_distance(customers[j]);
            }
        }
        q_bests = new Q_best[c_N];
        for(int i=0;i<c_N;i++)
        {
            double dis = 100000;
            int id = 0;
            for(int j=0;j<q_N;j++)
            {
                if(dis > customers[i].get_distance(chargestations[j]))
                {
                    dis = customers[i].get_distance(chargestations[j]);
                    id = j;
                }
                q_bests[i] = new Q_best(id,dis);
            }

        }
    }
    public static void main(String args[])
    {
        try
        {
            input("c101C5.txt");
        }
        catch(IOException e)
        {
        }
        for(Customer c:customers)
        {
            if(c!=null)
                c.print();
        }
    }
}
class Customer // the Customers
{
    String Type;
    String id;
    double x;
    double y;
    double r_time;
    double s_time;
    double d_time;
    double demand;

    public Customer(String Type,String id, double x, double y, double r_time, double s_time, double d_time, double demand) {
        this.Type = Type;
        this.id = id;
        this.x = x;
        this.y = y;
        this.r_time = r_time;
        this.s_time = s_time;
        this.d_time = d_time;
        this.demand = demand;
    }
    void print()
    {
        System.out.println(this.id);
        System.out.println(this.Type);
        System.out.println(this.x);
        System.out.println(this.y);
        System.out.println(this.demand);
        System.out.println(this.d_time);
        System.out.println(this.r_time);
        System.out.println(this.s_time);
    }
    double get_distance(Customer other)
    {
        return Math.sqrt((this.x-other.x)*(this.x-other.x)+(this.y-other.y)*(this.y-other.y));
    }
}
class Route
{
    double capacity;
    double dis;
    ArrayList<Integer> c_list = new ArrayList<>();// the customers list of vehicle
    boolean check()//check the feasible of the route
    {
        return check_c()&& check_t();
    }
    boolean check_c()
    {
        double c_capacity = 0;
        for(int i:c_list)
        {
            c_capacity += Conf.customers[i].demand;
        }
        return c_capacity < Conf.Q;
    }
    boolean check_t()
    {
        double fuel = 0;
        double time = 0;



    }
    double get_dis()
    {
        this.dis = 0;
        return this.dis;
    }

}

class Q_best // every customer has a best Q,when run ,if can't arrive next customers,then go to the best Q_best
{
    int i;
    double dis;
    public Q_best(int i, double dis) {
        this.i = i;
        this.dis = dis;
    }
}
class Solution
{
    ArrayList<Route> r_list = new ArrayList<>();
    double dis;
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
class Algorithm
{
    Solution get_ini_solution() // 获得初始解，使用最优插入算法
    {

    }
}
