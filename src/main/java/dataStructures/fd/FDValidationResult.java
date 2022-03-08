package dataStructures.fd;


import java.util.ArrayList;
import java.util.List;

public class FDValidationResult {

    public String status = "UNKNOWN";
    public List<Integer> violationRows;

    public FDValidationResult(){
        violationRows = new ArrayList<>();
    }

    public FDValidationResult(String status){
        this();
        this.status = status;
    }
}
