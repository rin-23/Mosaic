package shapes;

public class CPoint implements Comparable {

    public int x, y;
    public int id;
    public boolean isBoundary;
    
    public CPoint(int x, int y, int id, boolean isBoundary) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.isBoundary = isBoundary;
    }
    
    @Override
    public int compareTo(Object o) {
        int result;
        CPoint p = (CPoint) o;
        
        if (this.x > p.x) {
            result = 1;
        } else if (this.x < p.x) {
            result = -1;
        } else {
            if (this.y > p.y) {
                result = 1;
            } else if (this.y < p.y) {
                result = -1;
            } else {
                result = 0;
            }
        }
        
        if (result == 0 && this.id != -1) {
            if (this.id > p.id) {
                result = 1;
            } else if (this.id < p.id) {
                result = -1;
            } else {
                result = 0;
            }
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        return ("Point: x = " + x + ", y = " + y + ", id = " + id +
                ", is boundary = " + isBoundary);
    }
    
}
