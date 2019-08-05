import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;

public class BranchandBound {
    int numOfCity;
    Double[][] distMap;
    public BranchandBound( String name, int numOfCity)
    {
        this.numOfCity = numOfCity;
        Greedy gr = new Greedy(name, numOfCity);
        gr.genDistMatrix();
        this.distMap = gr.distMap;
    }
    public static class HeapNode implements Comparable{
        Double lcost;//子树费用的下界
        Double cc;//当前费用
        Double rcost;//x[s:n-1]中顶点最小出边费用和
        int s;//根节点到当前节点的路径为x[0:s]
        int[] x;//需要进一步搜索的顶点是x[s+1:n-1]

        //构造方法
        public HeapNode(Double lc,Double ccc,Double rc,int ss,int[] xx){
            lcost=lc;
            cc=ccc;
            s=ss;
            x=xx;
        }
        public int compareTo(Object x){
            Double xlc=((HeapNode) x).lcost;
            if(lcost<xlc) return -1;
            if(lcost==xlc) return 0;
            return 1;
        }
    }
    public static void main(String[] args){
        BranchandBound bb = new BranchandBound("D:\\workspace\\IAlab\\src\\TSP10cities.tsp",10);
        bb.modifydist();
        Calendar calendar = Calendar.getInstance();
        long millis = calendar.getTimeInMillis();
        bb.solve();
        calendar = Calendar.getInstance();
        long runtime = calendar.getTimeInMillis()-millis;
        System.out.println("runtime: " + runtime + "ms");
    }

    public void modifydist(){
        Double[][] tmpDist = new Double[numOfCity+1][numOfCity+1];
        for (int i = 0;i<=numOfCity;i++){
            for (int j = 0;j<=numOfCity; j++){
                if(i == 0|| j ==0)
                    tmpDist[i][j] = 0.0d;
                else if(i == j)
                    tmpDist[i][j] = -1.0d;
                else
                    tmpDist[i][j] = distMap[i-1][j-1];
            }
        }
        distMap = tmpDist;
        tmpDist = null;
    }

    public void solve(){
        int []route = new int[numOfCity + 1];
        Double mindist = bbTsp(route);
        System.out.println("The route is: ");
        for (int i = 0;i<=numOfCity; i++){
            System.out.print(route[i]+" ");
        }
        System.out.println("\n"+"The minimun distance is: " + mindist);
    }
    public Double bbTsp(int[] v){
        int n=v.length-1;//节点数
        LinkedList<HeapNode> heap=new LinkedList<HeapNode>();
        Double[] minOut=new Double[n+1];
        Double minSum=0.0d;//最小出边费用和
        for(int i=1;i<=n;i++){//针对每个节点，找到最小出边
            //计算minOut[i]和minSum
            Double min=Double.MAX_VALUE;
            for(int j=1;j<=n;j++){
                if(distMap[i][j]<Double.MAX_VALUE&&distMap[i][j]<min)
                    min=distMap[i][j];
            }
            if(min==Double.MAX_VALUE)
                return Double.MAX_VALUE;
            minOut[i]=min;
            minSum+=min;
        }

        //初始化
        int[] x=new int[n];
        for(int i=0;i<n;i++)
            x[i]=i+1;
        HeapNode enode=new HeapNode(0.0d,0.0d,minSum,0,x);
        Double bestc=Double.MAX_VALUE;

        //搜索排列空间树
        while(enode!=null&&enode.s<n-1){
            //非叶节点
            x=enode.x;
            if(enode.s==n-2){
                //当前扩展结点是叶节点的父节点
                //再加两条边构成回路
                //所构成回路是否优于当前最优解
                if(distMap[x[n-2]][x[n-1]]!=-1&&distMap[x[n-1]][1]!=-1&&enode.cc+distMap[x[n-2]][x[n-1]]+distMap[x[n-1]][1]<bestc){
                    //找到费用更小的回路
                    bestc=enode.cc+distMap[x[n-2]][x[n-1]]+distMap[x[n-1]][1];
                    enode.cc=bestc;
                    enode.lcost=bestc;
                    enode.s++;
                    heap.add(enode);
                    Collections.sort(heap);
                }
            }else{//内部结点
                //产生当前扩展结点的儿子结点
                for(int i=enode.s+1;i<n;i++){
                    if(distMap[x[enode.s]][x[i]]!=-1){
                        //可行儿子结点
                        Double cc=enode.cc+distMap[x[enode.s]][x[i]];
                        Double rcost=enode.rcost=minOut[x[enode.s]];
                        Double b=cc+rcost;//下界
                        if(b<bestc){
                            //子树可能含有最优解，结点插入最小堆
                            int[] xx=new int[n];
                            for(int j=0;j<n;j++)
                                xx[j]=x[j];
                            xx[enode.s+1]=x[i];
                            xx[i]=x[enode.s+1];
                            HeapNode node=new HeapNode(b,cc,rcost,enode.s+1,xx);
                            heap.add(node);
                            Collections.sort(heap);
                        }
                    }
                }

            }

            //取下一个扩展结点
            enode=heap.poll();
        }
        //将最优解复制到v[1...n]
        for(int i=0;i<n;i++)
            v[i+1]=x[i];
        return bestc;
    }
}
