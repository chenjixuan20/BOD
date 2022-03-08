package discoverer.od;

import dataStructures.DataFrame;
import dataStructures.fd.FDCandidate;
import dataStructures.od.ODTree;

import java.util.List;

public abstract class ODDiscoverer {
    public abstract ODTree discover(DataFrame data, ODTree reference);
//    public final ODTree discover(DataFrame data){
//        return discover(data,null);
//    }
    public abstract ODTree discoverFD(DataFrame data, List<FDCandidate> fdCandidates);
}
