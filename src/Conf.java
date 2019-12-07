import java.awt.desktop.SystemSleepEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.CheckedOutputStream;

public class Conf {
    static int c_N=100;// the number of customers
    static int q_N=25;// the number of recharge station
    static double dis_m[][];// cost_m
    static double Q;// Vehicle fuel tank capacity
    static double C;// Vehicle load capacity
    static double r;// fuel consumption rate
    static double g;// inverse refueling rate
    static double v;// average Velocity
    static Customer customers[] = new Customer[c_N+1+q_N];
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
                chargestations[j] = new Customer(Type,ID, x, y, r_time, s_time, d_time, demand);
                customers[i] = chargestations[j];
                i++;
                j++;
            } else {
                customers[i] = new Customer(Type, ID ,  x, y, r_time, s_time, d_time, demand);
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
            e.printStackTrace();
        }
        initialize();
        ArrayList<Integer> c = new ArrayList<>();
        c.add(5);
        c.add(4);
        Route test_route = new Route(c);
        test_route.print();


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
    double dis;
    ArrayList<Integer> c_list = new ArrayList<>();// the customers list of vehicle
    Route(ArrayList<Integer> c_list)
    {
        this.c_list.addAll(c_list);
        this.dis = this.get_dis();
    }
    boolean check()//check the feasible of the route
    {
        return check_c() && check_t() && check_p();
    }
    boolean check_c()
    {
        return get_c_value() == 0;
    }
    boolean check_t() { return get_t_value() == 0; }
    boolean check_p() {  return get_v_value() == 0; }

    double get_c_value() // the vialation of capacity
    {
        double c_capacity = 0;
        for(int i:c_list)
        {
            c_capacity += Conf.customers[i].demand;
        }
        if (c_capacity - Conf.Q <= 0)
            return 0;
        else
            return c_capacity - Conf.Q;
    }
    double get_t_value() // the valation of time
    {
            double a[] = new double[c_list.size()];
            a[0] = Conf.customers[0].r_time;
            for(int i=1;i<c_list.size();i++)
            {
                a[i] = get_t_a(i,a);
            }
            double sum = 0;
            for(int i=0;i<c_list.size();i++)
            {
                sum += Math.max(0,a[i]);
            }
            return sum;

    }
    double get_t_a(int i, double [] a )//按照c_list 里来,递归为其赋值
    {

            double a1 = 0;
            a1 = a[0] + Conf.customers[c_list.get(i-1)].s_time+Conf.dis_m[c_list.get(i-1)][c_list.get(i)];
            if(a1 <= Conf.customers[c_list.get(i)].d_time)
                return Math.max(a1,Conf.customers[c_list.get(i)].r_time);
            else
                return Conf.customers[c_list.get(i)].d_time;
    }

    double get_v_value()// 电量约束
    {
        double a_forward[] = new double[c_list.size()];
        double a_backward[] = new double[c_list.size()];
        for(int i=0;i<c_list.size();i++)
        {
            a_forward[i] = get_v_forward(i,a_forward);
        }
        for (int i=c_list.size()-1;i>0;i--)
        {
            a_backward[i] = get_v_backward(i, a_backward);
        }
        double ans = 0;
        for(int i=0;i<c_list.size()-1;i++)
        {
            ans += Math.max(0,a_forward[i]-Conf.Q);
        }
        return ans;
    }
    double get_v_forward(int i, double [] a_forward)
    {
        if (i == 0)
        {
            return Conf.r * Conf.dis_m[c_list.get(i)][0];
        }
        else if (Conf.customers[c_list.get(i-1)].Type.equals("f"))
        {
            return Conf.r * Conf.dis_m[c_list.get(i)][c_list.get(i-1)];
        }
        else
        {
            return a_forward[i-1] + Conf.r * Conf.dis_m[c_list.get(i)][c_list.get(i-1)];
        }
    }
    double get_v_backward(int i, double [] a_backward)
    {
        if(i == c_list.size()-1)
        {
            return Conf.r * Conf.dis_m[c_list.get(i)][0];
        }
        else if (Conf.customers[c_list.get(i+1)].Type.equals("f"))
        {
            return Conf.r * Conf.dis_m[c_list.get(i)][c_list.get(i+1)];
        }
        else
        {
            return a_backward[i+1] + Conf.r * Conf.dis_m[c_list.get(i)][c_list.get(i-1)];
        }
    }
    double get_dis() // return the dis
    {
        this.dis = 0;
        this.dis += Conf.dis_m[0][c_list.get(0)];
        for(int i=0;i<this.c_list.size()-1;i++)
        {
            this.dis += Conf.dis_m[c_list.get(i)][c_list.get(i+1)];
        }
        this.dis += Conf.dis_m[c_list.get(c_list.size()-1)][0];
        return this.dis;
    }
    void print()
    {
        System.out.println("该路径为");
        System.out.print("depot-");
        for(Integer c:c_list)
        {
            System.out.print(Conf.customers[c].Type+"-");
        }
        System.out.print("depot");
        System.out.println();
        System.out.println("该route的路径长为"+dis);
        System.out.println("该route的容量惩罚值为"+this.get_c_value());
        System.out.println("该route的时间惩罚值为"+this.get_t_value());
        System.out.println("该route的电量惩罚值为"+this.get_v_value());
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

    Solution get_ini_solution_NNH() // 获得初始解，使用最优插入算法
    {
        Solution ini_solution = new Solution();
        return ini_solution;
    }
}
