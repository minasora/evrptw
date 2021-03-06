import javax.sound.midi.Soundbank;
import java.awt.*;
import java.awt.desktop.SystemSleepEvent;
import java.io.*;
import java.rmi.activation.ActivationGroup_Stub;
import java.util.*;
import java.util.zip.CheckedOutputStream;

public class Conf {
    static int c_N = 100;// the number of customers
    static int q_N = 25;// the number of recharge station
    static double dis_m[][];// cost_m
    static double Q;// Vehicle fuel tank capacity
    static double C;// Vehicle load capacity
    static double r;// fuel consumption rate
    static double g;// inverse refueling rate
    static double v;// average Velocity
    static ArrayList<Route> route_pool = new ArrayList(); // 路径池
    static ArrayList<Route> ppa_route_pool = new ArrayList<>(); // 部分路程池
    static ArrayList<RouteCustomer> ppa = new ArrayList<>();
    static Customer customers[] = new Customer[c_N + 1 + q_N];
    static Customer new_customers[] = new Customer[c_N + 1 + q_N];
    static Customer[] result_customers;

    static Customer chargestations[] = new Customer[q_N];
    static Q_best q_bests[];
    double[] select = new double[5];
    double[] particle = new double[5];
    static double alns_time;
    static double PFA_time;
    static double PPFA_time;
    static double APPFA_time;

    static void input(String file_name) throws FileNotFoundException {
        String ID;
        String Type;
        double x;
        double y;
        double demand;
        double r_time;
        double d_time;
        double s_time;

        File file = new File(file_name);
        Scanner cin = new Scanner(file);
        cin.nextLine();
        int i = 0;
        int j = 0;
        while (cin.hasNext()) {
            ID = cin.next();
            if (ID.equals("END"))
                break;
            Type = cin.next();
            x = cin.nextDouble();
            y = cin.nextDouble();
            demand = cin.nextDouble();
            r_time = cin.nextDouble();
            d_time = cin.nextDouble();
            s_time = cin.nextDouble();
            if (Type.equals("f")) {
                chargestations[j] = new Customer(j, Type, ID, x, y, r_time, s_time, d_time, demand);
                customers[i] = chargestations[j];
                i++;
                j++;
            } else {
                customers[i] = new Customer(i, Type, ID, x, y, r_time, s_time, d_time, demand);
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
        for (int i = 0; i < Conf.c_N; i++) {
            customers[i].num = i;
        }
        dis_m = new double[c_N][c_N];
        for (int i = 0; i < c_N; i++) {
            for (int j = 0; j < c_N; j++) {
                dis_m[i][j] = customers[i].get_distance(customers[j]);
            }
        }
        q_bests = new Q_best[c_N];
        for (int i = 0; i < c_N; i++) {
            double dis = 100000;
            int id = 0;
            for (int j = 0; j < q_N; j++) {
                if (dis > customers[i].get_distance(chargestations[j])) {
                    dis = customers[i].get_distance(chargestations[j]);
                    id = j;
                }
                q_bests[i] = new Q_best(id, dis);
            }

        }
    }

    public static void main(String args[]) {
        output();

    }

    static void output() {
        try {
            int p  = Parameter.CUS;
            String path = "result.txt";
            FileOutputStream puts = new FileOutputStream(path, true);
            PrintStream out = new PrintStream(puts);
            System.setOut(out);
            File[] templist;
            if(Parameter.file_name.equals("all")) {
                File file = new File("evrptw_instances");

             templist = file.listFiles();
            }
            else
            {
                templist = new File[1];
                templist[0] = new File("evrptw_instances"+"/"+Parameter.file_name);
            }
            for (int i = 0; i < templist.length; i++) {
                System.out.println(templist[i].toString());
                input(templist[i].toString());

                Algorithm al = new Algorithm();
                Customer[] target_customers = new Customer[Conf.c_N + 1];
                System.out.println("服务顾客数" + p);
                int t = 10;
                double alns_time =0;
                double alns_dis = 0;
                double pfa_time = 0;
                double pfa_dis = 0;
                double ppfa_time = 0;
                double ppfa_dis = 0;
                double appfa_time = 0;
                double appfa_dis = 0;
                double alns_max_time = 0;
                double alns_min_time = 10000;
                double pfa_max_time = 0;
                double pfa_min_time = 10000;
                double ppfa_min_time = 10000;
                double ppfa_max_time = 0;
                double appfa_min_time = 10000;
                double appfa_max_time = 0;
                double alns_max_dis = 0;
                double alns_min_dis = 10000;
                double pfa_max_dis = 0;
                double pfa_min_dis = 10000;
                double ppfa_min_dis = 10000;
                double ppfa_max_dis = 0;
                double appfa_min_dis = 10000;
                double appfa_max_dis = 0;
                for(int j = 0;j<=10;j++) {
                    al.generate_new_customers(target_customers, p);
                    Conf.customers = target_customers;
                    Conf.result_customers = target_customers;
                    Conf.c_N = Conf.q_N + p + 1;
                    long s_time = System.currentTimeMillis();
                    Solution solution = al.get_result_solution(Parameter.iterations);
                    long t_time = System.currentTimeMillis();
                    alns_time += (t_time - s_time) / 1000.0;
                    alns_dis += solution.get_dis();
                    if(alns_min_time > (t_time - s_time) )
                    {
                        alns_min_time = (t_time - s_time) / 1000.0;
                    }
                    if(alns_max_time < (t_time - s_time) )
                    {
                        alns_max_time = (t_time - s_time) / 1000.0;
                    }
                    if(alns_min_dis > solution.get_dis() )
                    {
                        alns_min_dis = solution.get_dis();
                    }
                    if(alns_max_dis < solution.get_dis() )
                    {
                        alns_max_dis = solution.get_dis();
                    }
                    Conf.c_N = 122;
                    Conf.customers = Conf.new_customers;
                    Solution solution1 = al.PFA(t, p);
                    pfa_dis += solution.get_dis();
                    pfa_time += Conf.PFA_time;
                    if(pfa_min_time > Conf.PFA_time )
                    {
                        pfa_min_time = (Conf.PFA_time);
                    }
                    if(pfa_max_time < Conf.PFA_time )
                    {
                        pfa_max_time = Conf.PFA_time ;
                    }
                    if(pfa_min_dis > solution1.get_dis() )
                    {
                        pfa_min_dis = solution1.get_dis();
                    }
                    if(pfa_max_dis < solution1.get_dis() )
                    {
                        pfa_max_dis =  solution1.get_dis();
                    }

                    Conf.c_N = 122;
                    Conf.customers = Conf.new_customers;
                    Solution solution2 = al.PPFA(p);
                    ppfa_dis += solution2.get_dis();
                    ppfa_time += Conf.PPFA_time;
                    if(ppfa_min_time > Conf.PPFA_time )
                    {
                        ppfa_min_time = (Conf.PFA_time);
                    }
                    if(ppfa_max_time < Conf.PPFA_time )
                    {
                        ppfa_max_time = Conf.PPFA_time ;
                    }
                    if(ppfa_min_dis > solution2.get_dis() )
                    {
                        ppfa_min_dis = solution2.get_dis();
                    }
                    if(ppfa_max_dis < solution2.get_dis() )
                    {
                        ppfa_max_dis =  solution2.get_dis();
                    }



                    Conf.c_N = 122;
                    Conf.customers = Conf.new_customers;
                    Solution solution3 = al.APPFA(p);

                    appfa_dis += solution3.get_dis();
                    appfa_time += Conf.APPFA_time;
                    if(appfa_min_time > Conf.APPFA_time )
                    {
                        appfa_min_time = (Conf.APPFA_time);
                    }
                    if(appfa_max_time < Conf.APPFA_time )
                    {
                        appfa_max_time = Conf.APPFA_time ;
                    }
                    if(appfa_min_dis > solution3.get_dis() )
                    {
                        appfa_min_dis = solution3.get_dis();
                    }
                    if(appfa_max_dis < solution3.get_dis() )
                    {
                        appfa_max_dis =  solution3.get_dis();
                    }
                    Conf.c_N = 122;
                    Conf.customers = Conf.new_customers;
                }
                System.out.println("alns求解时间"+alns_time / 10.0);
                System.out.println("alns求解花费"+alns_dis / 10.0);
                System.out.println("alns求解最好时间"+alns_min_time );
                System.out.println("alns求解最好花费"+alns_min_dis);
                System.out.println("alns求解最坏时间"+alns_max_time );
                System.out.println("alns求解最坏花费"+alns_max_dis );

                System.out.println();
                System.out.println("pfa求解时间"+pfa_time / 10.0);
                System.out.println("pfa求解花费"+pfa_dis / 10.0);
                System.out.println("pfa求解最好时间"+pfa_min_time );
                System.out.println("pfa求解最好花费"+pfa_min_dis);
                System.out.println("pfa求解最坏时间"+pfa_max_time );
                System.out.println("pfa求解最坏花费"+pfa_max_dis);
                System.out.println("pfa求解时间相对alns百分比"+(pfa_time/alns_time)*100+"%");
                System.out.println("pfa求解花费相对alns百分比"+(pfa_dis/alns_dis)*100+"%");

                System.out.println();
                System.out.println("ppfa求解时间"+ppfa_time / 10.0);
                System.out.println("ppfa求解花费"+ppfa_dis / 10.0);
                System.out.println("ppfa求解最好时间"+ppfa_min_time );
                System.out.println("ppfa求解最好花费"+ppfa_min_dis );
                System.out.println("ppfa求解最坏时间"+ppfa_max_time );
                System.out.println("ppfa求解最坏花费"+ppfa_max_dis );
                System.out.println("pfa求解时间相对alns百分比"+(ppfa_time/alns_time)*100+"%");
                System.out.println("pfa求解花费相对alns百分比"+(ppfa_dis/alns_dis)*100+"%");

                System.out.println();
                System.out.println("appfa求解时间"+appfa_time / 10.0);
                System.out.println("appfa求解花费"+appfa_dis / 10.0);
                System.out.println("appfa求解最好时间"+appfa_min_time );
                System.out.println("appfa求解最好花费"+appfa_min_dis);
                System.out.println("appfa求解最坏时间"+appfa_max_time );
                System.out.println("appfa求解最坏花费"+appfa_max_dis );
                System.out.println("pfa求解时间相对alns百分比"+(appfa_time/alns_time)*100+"%");
                System.out.println("pfa求解花费相对alns百分比"+(appfa_dis/alns_dis)*100+"%");
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class Customer implements Comparable<Customer>// the Customers
{
    String Type;
    String id;
    int num;
    double x;
    double y;
    double r_time;
    double s_time;
    double d_time;
    double demand;
    int true_id;


    public Customer(int num,String Type,String id, double x, double y, double r_time, double s_time, double d_time, double demand) {
        this.Type = Type;
        this.id = id;
        this.x = x;
        this.y = y;
        this.r_time = r_time;
        this.s_time = s_time;
        this.d_time = d_time;
        this.demand = demand;
        this.num = num;
        this.true_id = num;
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
    public int compareTo(Customer other){
        if(this.r_time>other.r_time)return 1;
        else return -1;

    }
}
class Route
{
    double dis;
    ArrayList<Integer> c_list = new ArrayList<>();// the customers list of vehicle
    double [] v;
    Route(ArrayList<Integer> c_list)
    {
        this.c_list.addAll(c_list);
        this.dis = this.get_dis();
    }
    Route()
    {

    }
    int size()
    {
        return c_list.size();
    }
    int best_insert_pos(int c)// only think about dis;
    {
        int i = -1;
        double ans = 10000;
        for(int j=0; j<c_list.size()+1;j++)
        {
            this.get_dis();
            double old_dis = this.dis;
            this.c_list.add(j,c);
            if(this.check())
            {
               double  res = this.get_dis() - old_dis;
               if (res < ans)
               {
                   i = j;
                   ans = res;
               }

            }
            this.c_list.remove(Integer.valueOf(c));
        }
        //if(this.c_list.size()==0)i = 0;
        return i;

}
    boolean check()//check the feasible of the route
    {
        return check_c() && check_t();
    }
    boolean check_c()
    {
        return get_c_value() == 0;
    }
    boolean check_t() { return get_t_value() == 0; }
    boolean check_p(int j) {
        v = new double[j+2];
        v[0] = Conf.Q;
        for(int i=1;i<=j+1;i++)
        {
            v[i] = -10;
        }
        for(int i=1;i<=j;i++) {

                if (i == 1) {
                    v[i] = v[0] - Conf.dis_m[0][this.c_list.get(i - 1)];
                }
                else
                {
                    v[i] = v[i-1] - Conf.dis_m[this.c_list.get(i-1)][this.c_list.get(i-2)];
                }
            if (v[i] < 0) {
                return false;
            }
            if(Conf.customers[this.c_list.get(i-1)].Type.equals("f")) {
                    v[i] = Conf.Q;
                }
            }
        v[j+1] = v[j] - Conf.dis_m[this.c_list.get(this.c_list.size()-1)][0];
        if(v[j+1]<0)
            return false;

        return true;

    }

    double get_c_value() // the vialation of capacity
    {
        if(this.c_list.size()==0)return 0;
        double c_capacity = 0;
        for(int i:c_list)
        {
            c_capacity += Conf.customers[i].demand;
        }
        if (c_capacity - Conf.C <= 0)
            return 0;
        else
            return c_capacity - Conf.C;
    }
    double get_t_value() // the valation of time
    {
            if(this.c_list.size()==0)return 0;
            double a[] = new double[c_list.size()+1];
            a[0] = Conf.customers[0].r_time;
            double sum = 0;
            for(int i=0;i<c_list.size();i++)
            {
                //System.out.println(a[i]);
                sum += get_t_a(i,a);
            }

            return sum;

    }
    double get_t_a(int i, double [] a )//按照c_list 里来,递归为其赋值
    {

            double charge_time = 0;
            if(i==0)
            {
                a[i] = Math.max(Conf.customers[c_list.get(0)].r_time,Conf.dis_m[c_list.get(0)][c_list.get(i)]);
                return 0;
            }
            if(Conf.customers[c_list.get(i)].Type.equals("f")) // 充电站得特殊处理
            {
                double sum = 0;
                for(int j = i;j>0;j--)
                {
                    sum += Conf.dis_m[c_list.get(j)][c_list.get(j-1)];
                    if(Conf.customers[c_list.get(j)].Type.equals("f") ) // 找到了上一个充电站和depot
                    {
                        break;
                    }
                    if(sum > Conf.Q)
                    {
                        sum = Conf.Q;
                    }
                    charge_time = sum/Conf.g;
                }

            }
            double a1 = 0;
            double v = 0;
            a1 = a[i-1] + Conf.customers[c_list.get(i-1)].s_time+Conf.dis_m[c_list.get(i-1)][c_list.get(i)] + charge_time;
            if(a1 <= Conf.customers[c_list.get(i)].d_time)
                a[i] =  Math.max(a1,Conf.customers[c_list.get(i)].r_time);

            else {
                a[i] = Conf.customers[c_list.get(i)].d_time;
                v = a1 - Conf.customers[c_list.get(i)].d_time;
            }
            return v;
    }

    double get_v_value()// 电量约束
    {
        if(this.c_list.size()==0)return 0;
        double []a_forward = new double[c_list.size()+1];
        double []a_backward = new double[c_list.size()+1];
        for(int i=0;i<=c_list.size();i++)
        {
            a_forward[i] = get_v_forward(i,a_forward);
        }
        for (int i=c_list.size()-1;i>0;i--)
        {
            a_backward[i] = get_v_backward(i, a_backward);
        }
        double ans = 0;

        for(int i=0;i<=c_list.size();i++)
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
            if(i==c_list.size())
                return Conf.r*Conf.dis_m[c_list.get(i-1)][0];
            return Conf.r * Conf.dis_m[c_list.get(i)][c_list.get(i-1)];
        }
        else
        {
            if(i==c_list.size())
                return a_forward[i-1] + Conf.r*Conf.dis_m[c_list.get(i-1)][0];
            else
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
        if(this.c_list.size()==0)return this.dis;
        this.dis += Conf.dis_m[0][c_list.get(0)];
        for(int i=0;i<this.c_list.size()-1;i++)
        {
            this.dis += Conf.dis_m[c_list.get(i)][c_list.get(i+1)];
        }
        this.dis += Conf.dis_m[c_list.get(c_list.size()-1)][0];
        return this.dis;
    }
    Route deepcopy()
    {
        Route new_route = new Route();
        new_route.c_list.addAll(this.c_list);
        new_route.get_dis();
        return new_route;
    }
    void print()
    {
        System.out.println("该路径为");
        System.out.print("depot-");
        for(Integer c:c_list)
        {
            System.out.print(Conf.customers[c].id+"-");
        }
        System.out.print("depot");
        System.out.println();
        System.out.println("该route的路径长为"+dis);
        System.out.println("该route的容量惩罚值为"+this.get_c_value());
        System.out.println("该route的时间惩罚值为"+this.get_t_value());
        System.out.println("该route的电量惩罚值为"+this.get_v_value());

    }
    void find_best_station_insert()
    {
        int result_i = 0;
        if(this.check_p(this.c_list.size()))
            return;
        for(int i=0;i<this.c_list.size()+1;i++)
        {
            if(this.v[i+1]<0)
            {
                result_i = i;
                break;

            }
        }
        while(true) {
            if(result_i == -1)return;
            int t = find_best_station(result_i);

            if (t == -1)
            {
                result_i --;
            }
            else
            {
                this.c_list.add(result_i, t);
                break;
            }
        }

    }

    int find_best_station(int i)
    {
        int result = -1;
        double ans = 1000;
        for(int j=1;j<=Conf.q_N;j++)
        {
            dis = this.get_dis();
            this.c_list.add(i,Conf.customers[j].num);
            double a;
            if(i == 0)
                 a = this.v[i]-Conf.dis_m[0][this.c_list.get(i)];
            else
                a = this.v[i]-Conf.dis_m[this.c_list.get(i-1)][this.c_list.get(i)];
             if(this.check() && (a > 0 )  )
            {
                if(ans > this.get_dis() - dis)
                {
                    result = Conf.customers[j].num;
                }
            }
            this.c_list.remove(i);
        }

        return result;
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
    ArrayList<Integer> relaxed_clist =  new ArrayList<>();
    ArrayList<Integer> unrelaxed_clist = new ArrayList<>();

    double dis;
    double fitness;
    boolean check()
    {
        for (Route r:r_list)
        {
            if(!r.check())
                return false;
        }
        return true;
    }
    double get_dis()
    {
        double a=0;
        for(Route r:this.r_list)
        {
            a += r.dis;
        }
        return a;
    }
    void set_dis()
    {
        this.dis = 0;
        for(Route r: this.r_list) {
            r.dis = r.get_dis();
            this.dis += r.dis;
        }
    }
    void set_fitness()
    {
        this.fitness = 0;
        for(Route r:this.r_list)
        {
            this.fitness += r.dis + r.get_v_value() + r.get_t_value() + r.get_c_value();
        }
    }
    void print()
    {
        this.set_dis();
        System.out.println("该解dis为"+this.dis);
        for(Route r:r_list)
        {
            r.print();
        }
    }
    Solution deepcopy()
    {
        Solution new_solution = new Solution();
        new_solution.relaxed_clist.addAll(this.relaxed_clist);
        new_solution.unrelaxed_clist.addAll(this.unrelaxed_clist);
        for(Route a :this.r_list)
        {
            Route route = a.deepcopy();
            new_solution.r_list.add(route);
        }
        new_solution.dis = this.dis;
        new_solution.fitness = this.fitness;
        return new_solution;
    }
    int size()
    {
        int ans = 0;
        for(Route r : this.r_list)
        {
            ans += r.size();
        }
        return ans;
    }
    void remove(int i)
    {
        int t = -1;
        for(Route r:this.r_list)
            for(int j:r.c_list)
            {
                if(i==j)
                {
                    r.c_list.remove(Integer.valueOf(i));
                    this.set_dis();
                    this.set_fitness();
                    return;
                }

            }

    }
}

class RouteCustomer
{
    double length;
    Customer customer;
    Route route;
    RouteCustomer deepcopy()
    {
        RouteCustomer new_route_customer = new RouteCustomer();
        new_route_customer.length = this.length;
        new_route_customer.route = this.route.deepcopy();
        new_route_customer.customer = this.customer;
        return new_route_customer;
    }
}

class AngelCustomer implements Comparable<AngelCustomer>
{
    double angel;
    int id;
    Customer customer;
    AngelCustomer(Customer customer,int id)
    {
        this.id = id;
        this.customer = customer;
    }
    void set_angel(AngelCustomer order_customer)// 以指定点和原点作为轴计算角度
    {

        double l1 = Conf.dis_m[this.id][0];
        double l2 = Conf.dis_m[0][order_customer.id];
        double l3 = Conf.dis_m[this.id][order_customer.id];
        this.angel = (l1 * l1 + l2 * l2 - l3 * l3) / (2 * l1 * l2);
    }
    @Override
    public int compareTo(AngelCustomer other)
    {
        if (this.angel > other.angel)
            return 1;
        else return -1;
    }


}
class Pos // 决定插入位置
{
    int route_id;
    int pos_i;
    public Pos(int route_id, int pos_i) {
        this.route_id = route_id;
        this.pos_i = pos_i;
    }
}

class Algorithm {
    Pos find_best_Pos(Solution solution, int c) {
        int i = -1;
        double ans = 10000;
        int route_id = -1;
        int result_i = -1;
        for (int t = 0; t < solution.r_list.size(); t++) {
            Route cur = solution.r_list.get(t).deepcopy();
            if (solution.r_list.get(t).best_insert_pos(c) != -1) {
                i = solution.r_list.get(t).best_insert_pos(c);
                cur.c_list.add(i, c);
                cur.get_dis();
                if (ans > cur.dis - solution.r_list.get(t).dis) {
                    ans = cur.dis - solution.r_list.get(t).dis;
                    route_id = t;
                    result_i = i;
                }
            }
        }
        if (result_i == -1) {
            return new Pos(-1, -1);
        } else
            return new Pos(route_id, result_i);

    }

    int get_random_int(int i, int j) {
        Random r = new Random();
        int ans = i + r.nextInt(j - i);
        return ans;
    }

    ArrayList<AngelCustomer> get_sort_customers() {
        ArrayList<AngelCustomer> angelCustomers = new ArrayList<>();
        for (int i = Conf.q_N + 1; i < Conf.c_N; i++) {
            angelCustomers.add(new AngelCustomer(Conf.customers[i], i));
        }
        int order_id = get_random_int(0, angelCustomers.size());
        for (int i = 0; i <= angelCustomers.size() - 1; i++) {
            angelCustomers.get(i).set_angel(angelCustomers.get(order_id));
        }
        Collections.sort(angelCustomers);
        return angelCustomers;
    }

    Solution get_ini_solution_time() {

        ArrayList<Customer> time_customers = new ArrayList<>();

        for (int i = Conf.q_N + 1; i < Conf.c_N; i++) {
            time_customers.add(Conf.customers[i]);
        }
        Collections.sort(time_customers);
        boolean if_new_route = false;
        Route route = new Route();
        Solution solution = new Solution();
        for (int i = 0; i < time_customers.size(); i++) {
            solution.unrelaxed_clist.add(time_customers.get(i).num);
        }
        while (time_customers.size() != 0) {
            if (if_new_route) {
                route.get_dis();
                solution.r_list.add(route.deepcopy());
                route.c_list.clear();

            }
            if_new_route = true;
            Iterator<Customer> iterator = time_customers.iterator();
            while (iterator.hasNext()) {
                Customer c = iterator.next();
                route.c_list.add(c.num);
                boolean if_insert = route.check();
                route.c_list.remove(Integer.valueOf(c.num));
                if (if_insert) {
                    route.c_list.add(c.num);
                    iterator.remove();
                    route.get_dis();
                    if_new_route = false;
                    break;
                }
            }
        }
        solution.r_list.add(route);

        solution.set_dis();
        return solution;


    }

    Solution get_ini_solution_NNH()// 获得初始解，使用最优插入算法
    {
        Solution ini_solution = new Solution();
        ArrayList<AngelCustomer> sorted_customer = get_sort_customers();

        Route route = new Route();
        route.c_list.add(sorted_customer.get(0).id);
        ini_solution.unrelaxed_clist.add(sorted_customer.get(0).id);
        sorted_customer.remove(sorted_customer.get(0));
        ini_solution.r_list.add(route.deepcopy());
        route.c_list.clear();
        for (AngelCustomer c : sorted_customer) {
            Pos pos = find_best_Pos(ini_solution, c.id);
            int i = pos.pos_i;
            if (i == -1) {
                route.c_list.add(c.id);
                ini_solution.unrelaxed_clist.add(c.id);
                route.get_dis();
                ini_solution.r_list.add(route.deepcopy());
                route.c_list.clear();

            } else {
                ini_solution.r_list.get(pos.route_id).c_list.add(pos.pos_i, c.id);
                ini_solution.unrelaxed_clist.add(c.id);
            }
        }

        ini_solution.set_dis();

        return ini_solution;
    }

    Solution random_remove_customers(Solution solution) {
        int p = Parameter.CUS/5;
        while (solution.relaxed_clist.size() != p) {
            int i = get_random_int(0, solution.unrelaxed_clist.size());
            solution.relaxed_clist.add(solution.unrelaxed_clist.get(i));
            solution.remove(solution.unrelaxed_clist.get(i));
            solution.unrelaxed_clist.remove(i);

        }
        return solution;

    }

    Solution greedy_insert_customers(Solution solution) // 贪婪法重新插回
    {
        double ans = 10000;
        int result_i = -1;
        Pos result_pos = new Pos(-1, -1);
        for (int i : solution.relaxed_clist) {
            double dis = solution.dis;

            Pos pos = find_best_Pos(solution, i);
            if (pos.pos_i == -1)
                return solution;
            solution.r_list.get(pos.route_id).c_list.add(pos.pos_i, i);
            solution.set_dis();
            if (ans > solution.dis - dis) {
                result_i = i;
                result_pos = pos;
            }
            solution.r_list.get(pos.route_id).c_list.remove(pos.pos_i);
        }
        solution.r_list.get(result_pos.route_id).c_list.add(result_pos.pos_i, result_i);
        solution.unrelaxed_clist.add(result_i);
        solution.relaxed_clist.remove(Integer.valueOf(result_i));
        solution.set_dis();
        return solution;
    }
    // Solution find_best_charge_station()
    //  {

    // }

    Solution large_neigh_search(Solution solution) {
        double dis = solution.dis;
        solution = random_remove_customers(solution);
        solution.set_dis();
        while (solution.relaxed_clist.size() != 0) {
            if (solution.dis >= dis) break;
            solution.set_dis();
            double old_dis = solution.dis;
            solution = greedy_insert_customers(solution);
            solution.set_dis();
            if (solution.dis == old_dis) // 剪枝
                break;
        }
        return solution;
    }


    Solution station_pair(Solution solution) // 满足电量约束//修复不可行解
    {
        for (Route r : solution.r_list) {
            boolean flag = r.check_p(r.c_list.size());
                r.find_best_station_insert();
                flag = r.check_p(r.c_list.size());
                if(!flag)
                {
                    r.find_best_station_insert();
                }
            }

        return solution;
    }

    Solution PFA(int h,int p) {
        Customer[] target_customers = new Customer[Conf.c_N + 1];
        Boolean[] sign = new Boolean[Conf.c_N+1];
        int t = Conf.c_N;
        for(int i=1;i<=Parameter.efforts_iterations;i++) {
            generate_new_customers(target_customers, 50);
            get_route_pool(target_customers,50,t);
        }
        generate_new_customers(target_customers,p);
        Conf.customers = Conf.result_customers;
        Conf.c_N = Conf.q_N+p+1;
        Route [] routes = new Route[Parameter.route_pool_number];
        ArrayList end_customers = new ArrayList();
        Solution solution = new Solution();
        for(RouteCustomer r: Conf.ppa) {
            if (if_all_in(r.route, target_customers, sign)) {
                solution.r_list.add(r.route.deepcopy());
                for (Integer i  : r.route.c_list)
                {
                    for (int j = 0; j <= Conf.c_N; j++) {
                        if(i == Conf.customers[j].true_id) {
                            sign[j] = true;
                            break;
                        }
                    }
                }

            }
        }

        long startTime = System.currentTimeMillis();
        solution = get_result_solution(7500);
        long endTime   = System.currentTimeMillis();
        long TotalTime = endTime - startTime;
        Conf.PFA_time = TotalTime/1000.0;
        return solution;
    }
    Solution PPFA(int p)
    {
        Solution solution = new Solution();
        Customer[] target_customers = new Customer[Conf.c_N + 1];
        Boolean[] sign = new Boolean[Conf.c_N+1];
        int t = Conf.c_N;
        generate_new_customers(target_customers,p);
        Conf.customers = Conf.result_customers;
        Conf.c_N = Conf.q_N+p+1;
        for(RouteCustomer r: Conf.ppa) {
            if (if_all_in(r.route, target_customers, sign)) {
                solution.r_list.add(r.route.deepcopy());
                for (Integer i  : r.route.c_list)
                {
                    for (int j = 0; j <= Conf.c_N; j++) {
                        if(i == Conf.customers[j].true_id) {
                            sign[j] = true;
                            break;
                        }
                    }
                }

            }
        }
        int k = Parameter.efforts_iterations;
        long startTime = System.currentTimeMillis();
        solution = get_result_solution(1500+k);
        long endTime   = System.currentTimeMillis();
        long TotalTime = endTime - startTime;
        Conf.PPFA_time = TotalTime/1000.0;
        return solution;
    }
    Solution APPFA(int p)
    {
        double []select = new double[5];
        double []particle = new double[5];
        double ans_s = 0;
        double ans_p = 0;
        for(int i=0;i<5;i++)
        {
            ans_s += select[i];
            ans_p += select[i];
        }
        Customer[] target_customers = new Customer[Conf.c_N + 1];
        Boolean[] sign = new Boolean[Conf.c_N+1];
        int t = Conf.c_N;
        generate_new_customers(target_customers,p);
        Conf.customers = Conf.result_customers;
        Conf.c_N = Conf.q_N+p+1;

        Random r = new Random();
        double g = r.nextDouble();
        double res_i = 0;
        double res_j = 0;
        for(int i=0;i<=4;i++)
        {
            res_i+=select[i]/ans_s;
            if(res_i>g)
            {
                res_i = i;
                break;
            }

        }
        for(int i=0;i<=4;i++)
        {
            res_j+=particle[i]/ans_s;
            if(res_i>g)
            {
                res_j = i;
                break;
            }

        }
        ArrayList end_customers = new ArrayList();
        int k = Parameter.efforts_iterations;
        Solution solution = new Solution();
        RouteCustomer []rs = new RouteCustomer[Conf.c_N];
        if(t>1) {
            switch ((int) res_i) {
                case 1:
                    part_route_by_length(5);
                case 2:
                    part_route_by_time(500);
                case 3:
                    part_route_by_cost(50);
                case 4:
                    part_route_by_charge(66);
            }
            switch ((int) res_j) {
                case 1:
                    select_by_cost_less(rs);
                case 2:
                    select_by_cost_more(rs);
                case 3:
                    select_by_more_time(rs);
                case 4:
                    select_by_nodes_long(rs);

            }
        }
        long startTime = System.currentTimeMillis();
        solution = get_result_solution(2000+k);
        long endTime   = System.currentTimeMillis();
        long TotalTime = endTime - startTime;
        Conf.APPFA_time = TotalTime/1000.0;
        return solution;




    }
    boolean if_all_in(Route r,Customer [] customers,Boolean[] sign)
    {
        boolean flag= false;
        for(int i:r.c_list) {
            if (Conf.customers[i].Type.equals("f")) {
                for (int j = 0; j < Conf.q_N + 25; j++) {
                    if (i == Conf.customers[j].true_id && !sign[j]) // 没被用过
                        // {
                        flag = true;
                    break;
                }
            }
            if(!flag)
                return false;
        }



        return true;
    }


    Solution get_result_solution(int j)
    {
        Conf.initialize();
        Algorithm al = new Algorithm();
        Route route = new Route();
        Solution solution = al.get_ini_solution_time();
        Solution new_solution = solution.deepcopy();
        for(int i=1;i<=j;i++) {
            while (true) {
                al.large_neigh_search(solution);
                if (solution.relaxed_clist.size() == 0)
                    break;
                else {
                    solution = new_solution.deepcopy();
                }
            }
            if (solution.relaxed_clist.size() != 0 || solution.dis > new_solution.dis)
                solution = new_solution.deepcopy();
            else {
                new_solution = solution.deepcopy();
            }
            //System.out.println(solution.dis);
        }
        solution = al.station_pair(solution);
        solution.set_dis();

        return solution;
    }


    void generate_new_customers(Customer [] target_customers,int p) {
        Conf.new_customers = Conf.customers; // baocun
        int C_N = Conf.c_N;
        for (int i = 0; i < Conf.q_N; i++) {
            target_customers[i] = Conf.customers[i];
        }
        for (int i = Conf.q_N; i <= Conf.q_N + p; i++) {
            int j = get_random_int(0, Conf.c_N - Conf.q_N) + Conf.q_N + 1; // 随机生成顾客
            if (Arrays.asList(target_customers).contains(Conf.customers[j])) {
                i--;
                continue;
            } else {
                target_customers[i] = Conf.customers[j];
                }
            }

        }
        void get_route_pool(Customer []target_customers,int p, int C_N) {
            Conf.customers = target_customers;
            Conf.c_N = Conf.q_N+p+1;

            Solution solution = get_result_solution(100);
            for(Route r : solution.r_list) {
                if (r.check_p(r.c_list.size())) {
                    Route new_route = new Route();
                    for (int i : r.c_list) {

                        new_route.c_list.add(Conf.customers[i].true_id);

                    }
                    new_route.dis = r.dis;
                    Conf.route_pool.add(new_route); // 储存路径
                }
            }
            Conf.customers = Conf.new_customers;
            Conf.c_N = C_N;
        }
        void part_route_by_length(int j) // 按照g拆分
        {
            j = Parameter.portion_length;
            for (Route r : Conf.route_pool) {
                RouteCustomer route_customer = new RouteCustomer();
                Route route = new Route();
                if (r.c_list.size() <= j)// 比固定长度小
                {
                    route_customer.route = r;
                    route_customer.length = r.dis;
                    route_customer.customer = Conf.customers[r.c_list.get(r.c_list.size() - 1)];
                    Conf.ppa.add(route_customer.deepcopy());
                    route_customer = new RouteCustomer();
                }
                else {
                    int cnt = 0;
                    for (int i = 0; i < r.c_list.size();i++)
                    {
                        cnt++;
                        route.c_list.add(r.c_list.get(i));
                        if(cnt==j)
                        {
                            route_customer.route = route;
                            route_customer.length = route.get_dis();
                            route_customer.customer = Conf.customers[r.c_list.get(i)];
                        }
                    }
                }
            }
        }
        void part_route_by_cost(int j)
        {
            {
                for (Route r : Conf.route_pool) {
                    RouteCustomer route_customer = new RouteCustomer();
                    Route route = new Route();
                    if (r.dis<j)// 比固定长度小
                    {
                        route_customer.route = r;
                        route_customer.length = r.dis;
                        route_customer.customer = Conf.customers[r.c_list.get(r.c_list.size() - 1)];
                        Conf.ppa.add(route_customer.deepcopy());
                        route_customer = new RouteCustomer();
                    }
                    else {
                        double cnt = 0;
                        cnt = Conf.dis_m[0][r.c_list.get(0)];
                        for (int i = 0; i < r.c_list.size()-1;i++)
                        {
                            cnt += Conf.dis_m[r.c_list.get(i+1)][r.c_list.get(i)];
                            route.c_list.add(r.c_list.get(i));
                            if(cnt>=j)
                            {
                                route_customer.route = route;
                                route_customer.length = route.get_dis();
                                route_customer.customer = Conf.customers[r.c_list.get(i)];
                                cnt = 0;
                            }
                        }
                    }
                }
            }
        }
        void part_route_by_time(int j)
        {
            {
                for (Route r : Conf.route_pool) {
                    RouteCustomer route_customer = new RouteCustomer();
                    Route route = new Route();
                    if (r.dis<j)// 比固定时间小
                    {
                        route_customer.route = r;
                        route_customer.length = r.dis;
                        route_customer.customer = Conf.customers[r.c_list.get(r.c_list.size() - 1)];
                        Conf.ppa.add(route_customer.deepcopy());
                        route_customer = new RouteCustomer();
                    }
                    else {
                        double cnt = 0;
                        cnt = Conf.dis_m[0][r.c_list.get(0)];
                        for (int i = 0; i < r.c_list.size()-1;i++)
                        {
                            cnt += Conf.customers[r.c_list.get(i)].d_time;
                            route.c_list.add(r.c_list.get(i));
                            if(cnt>=j)
                            {
                                cnt += Conf.customers[r.c_list.get(i)].d_time;
                                route_customer.route = route;
                                route_customer.length = route.get_dis();
                                route_customer.customer = Conf.customers[r.c_list.get(i)];
                                cnt = 0;
                            }
                        }
                    }
                }
            }
            return;
        }
       void part_route_by_charge(int j)
        {
            {
                {
                    for (Route r : Conf.route_pool) {
                        RouteCustomer route_customer = new RouteCustomer();
                        Route route = new Route();
                        if (r.v[r.c_list.size()-1]<j)// 比固定时间小
                        {
                            route_customer.route = r;
                            route_customer.length = r.dis;
                            route_customer.customer = Conf.customers[r.c_list.get(r.c_list.size() - 1)];
                            Conf.ppa.add(route_customer.deepcopy());
                            route_customer = new RouteCustomer();
                        }
                        else {
                            double cnt = 0;
                            cnt = Conf.dis_m[0][r.c_list.get(0)];
                            for (int i = 0; i < r.c_list.size()-1;i++)
                            {
                                cnt += 1;
                                route.c_list.add(r.c_list.get(i));
                                if(cnt>=j)
                                {
                                    cnt = 0;
                                    route_customer.route = route;
                                    route_customer.length = route.get_dis();
                                    route_customer.customer = Conf.customers[r.c_list.get(i)];
                                    cnt = 0;
                                }
                            }
                        }
                    }
                }
            }

        }
        RouteCustomer select_by_nodes_short(RouteCustomer targets[])
        {
            RouteCustomer res = new RouteCustomer();
            int ans  = 1000000;
            for(int i=0;i<targets.length;i++)
            {
                if(ans>targets[i].route.size())
                {
                    res = targets[i];
                    ans = targets[i].route.size();
                }
            }
            return res;
        }
        RouteCustomer select_by_nodes_long(RouteCustomer targets[])
        {
            RouteCustomer res = new RouteCustomer();
            double  ans  = -1000000;
            for(int i=0;i<targets.length;i++)
            {
                if(ans>targets[i].route.size())
                {
                    res = targets[i];
                    ans = targets[i].route.size();
                }
            }
            return res;

        }
        RouteCustomer select_by_cost_more(RouteCustomer targets[])
        {
            RouteCustomer res = new RouteCustomer();
            double ans  = -1000000;
            for(int i=0;i<targets.length;i++)
            {
                if(ans>targets[i].route.dis)
                {
                    res = targets[i];
                    ans = targets[i].route.size();
                }
            }
            return res;
        }
        RouteCustomer select_by_cost_less(RouteCustomer targets[])
        {

                RouteCustomer res = new RouteCustomer();
                double  ans  = 1000000;
                for(int i=0;i<targets.length;i++)
                {
                    if(ans>targets[i].route.dis)
                    {
                        res = targets[i];
                        ans = targets[i].route.size();
                    }
                }

            return res;

        }
        RouteCustomer select_by_more_time(RouteCustomer targets[])
        {
            RouteCustomer res = new RouteCustomer();
            double ans  = 1000000;
            for(int i=0;ans<targets.length;i++)
            {
                if(ans<targets[i].customer.d_time)
                {
                    res = targets[i];
                    ans = targets[i].route.size();
                }
            }

            return res;
        }
        RouteCustomer select_by_less_time(RouteCustomer targets[])
        {
            RouteCustomer res = new RouteCustomer();
            double ans  = -1000000;
            for(int i=0;ans>targets[i].customer.d_time;i++)
            {
                if(ans<targets[i].customer.d_time)
                {
                    res = targets[i];
                    ans = targets[i].customer.d_time;
                }
            }

            return res;
        }


    }


