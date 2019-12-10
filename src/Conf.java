import javax.sound.midi.Soundbank;
import java.awt.desktop.SystemSleepEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.activation.ActivationGroup_Stub;
import java.util.*;
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
                chargestations[j] = new Customer(j,Type,ID, x, y, r_time, s_time, d_time, demand);
                customers[i] = chargestations[j];
                i++;
                j++;
            } else {
                customers[i] = new Customer(i,Type, ID ,  x, y, r_time, s_time, d_time, demand);
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
            input("c101_21.txt");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        initialize();
        Algorithm al = new Algorithm();
        Route route = new Route();
        Solution solution = al.get_ini_solution_time();
        solution.print();
        System.out.println(solution.size());
        solution.r_list.get(0).c_list.add(9);
        System.out.println(solution.r_list.get(0).check());
        Solution new_solution = solution.deepcopy();
        for(int i=1;i<=100000;i++)
        {
            while(true) {
                al.large_neigh_search(solution);
                if(solution.relaxed_clist.size()==0)
                    break;
                else
                {
                    solution = new_solution.deepcopy();
                }
            }
            if(solution.relaxed_clist.size()!=0 || solution.dis > new_solution.dis)
                solution = new_solution.deepcopy();
            else
            {
                new_solution = solution.deepcopy();
            }
            System.out.println(solution.dis);
        }
        solution.print();


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
    boolean check_p() {  return get_v_value() == 0; }

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
        for(int i=0;i<time_customers.size();i++)
        {
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
            while(iterator.hasNext()) {
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
            System.out.println(ini_solution.unrelaxed_clist.size());
            return ini_solution;
        }
        Solution random_remove_customers (Solution solution)
        {
            int p = 25;
            while (solution.relaxed_clist.size() != p) {
                int i = get_random_int(0, solution.unrelaxed_clist.size());
                solution.relaxed_clist.add(solution.unrelaxed_clist.get(i));
                solution.remove(solution.unrelaxed_clist.get(i));
                solution.unrelaxed_clist.remove(i);

            }
            return solution;

        }
        Solution greedy_insert_customers (Solution solution) // 贪婪法重新插回
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

        Solution large_neigh_search (Solution solution)
        {
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


    }



