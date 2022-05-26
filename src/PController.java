public class PController {

    public double target = 0;
    public double gain = 0;
    public static int calls_counter = 0;

    public PController(double target, double gain){
        this.target = target;
        this.gain = gain;
        System.out.println("PControllert target="+this.target+", gain="+this.gain);
    }

    public double get_controlled_var(double variable){
        double error = target - variable;
        if(error < 0) error = error*(-1); //??

        double proportion = error * gain;
        double output = proportion;

        calls_counter++;
        if (calls_counter%10 == 0)
            System.out.println("calcNN:"
                    +", time=" + calls_counter
                    +", variable=" + variable
                    +", error=" + error
                    +", proportion=" + proportion
                    +", output=" + output);
        return output;
    }

}
