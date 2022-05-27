import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class LandingSimulation{
//	I chose to use the actual telemetry data at attitude 30,012m as my initial state for the simulation
//	I took the data from the telemetry shown in live broadcast at 23:15
//	https://youtu.be/HMdUcchBYRA?t=1395
  	public static final double WEIGHT_EMP = 164; // kg. according to wikipedia
	public static final double WEIGHT_FULE = 585; // kg. actual fuel at attitude 30,012m
	public static final double WEIGHT_FULL = WEIGHT_EMP + WEIGHT_FULE; // kg
// https://davidson.weizmann.ac.il/online/askexpert/%D7%90%D7%99%D7%9A-%D7%9E%D7%98%D7%99%D7%A1%D7%99%D7%9D-%D7%97%D7%9C%D7%9C%D7%99%D7%AA-%D7%9C%D7%99%D7%A8%D7%97
	public static final double MAIN_ENG_F = 430; // N
	public static final double SECOND_ENG_F = 25; // N
	public static final double MAIN_BURN = 0.15; //liter per sec, 12 liter per m'
	public static final double SECOND_BURN = 0.009; //liter per sec 0.6 liter per m'
	public static final double ALL_BURN = MAIN_BURN + 8*SECOND_BURN;  

	public static double accMax(double weight) {
		return acc(weight, true,8);
	}
    
	public static double acc(double weight, boolean main, int seconds) {
		double t = 0;
		if(main) {t += MAIN_ENG_F;}
		t += seconds*SECOND_ENG_F;
		double ans = t/weight;
		return ans;
	}

	public static double clamp(double decimal_NN){
		if(decimal_NN < 0) return 0;
		if(decimal_NN > 1) return 1;
		return decimal_NN;
	}

	public static void main(String[] args) {
		try {
			FileWriter myWriter = new FileWriter("landing_data_log.csv");
			System.out.println("Simulating Bereshit's Landing:");
			// starting point:
			double vertical_velocity = 43; //actual at attitude 30,012m
			double horizontal_velocity = 1701.7;
			double dist = 181*1000;
			double ang = 58.3; // zero is vertical (as in landing)
			double alt = 13748; // 2:25:40 (as in the simulation) // https://www.youtube.com/watch?v=JJ0VfRL9AMs

			double time = 0;
			double sec = 1; // sec
			double acc=0; // Acceleration rate (m/s^2)
			double fuel = 121; //
			double weight = WEIGHT_EMP + fuel;
			DecimalFormat df2 = new DecimalFormat("#.##");

			System.out.println("time, vertical_velocity, horizontal_velocity, dist, alt, ang, weight, acc");
			myWriter.write("time,vertical_velocity,horizontal_velocity,dist,alt,ang,weight,acc,fuel,NN\n");
			double NN = 0.7; // rate[0,1]
			// ***** main simulation loop ******

			PController p_controller = new PController(0, 0.04);


			while(alt>0) {

				// over 13 km above the ground
				if(alt>13000) {	// maintain a vertical speed of [20-25] m/s
					if(vertical_velocity >25) {NN+=0.003*sec;} // more power for braking
					if(vertical_velocity <20) {NN-=0.003*sec;} // less power for braking
				}
				// lower than 13 km - horizontal speed should be close to zero
				else {
					if(ang>3) {ang-=3;} // rotate to vertical position.
					else {ang =0;}

					NN = clamp(p_controller.get_controlled_var(vertical_velocity));
					if(horizontal_velocity<2) {horizontal_velocity=0;}
				}

				// main computations
				double ang_rad = Math.toRadians(ang);
				double h_acc = Math.sin(ang_rad)*acc;
				double v_acc = Math.cos(ang_rad)*acc;
				double vacc = Moon.getAcc(horizontal_velocity);
				time+=sec;
				double dw = sec*ALL_BURN*NN;
				if(fuel>0) {
					fuel -= dw;
					weight = WEIGHT_EMP + fuel;
					acc = NN* accMax(weight);
				}
				else { // ran out of fuel
					acc=0;
				}

				v_acc -= vacc;
				if(horizontal_velocity>0) {horizontal_velocity -= h_acc*sec;}
				dist -= horizontal_velocity*sec;
				vertical_velocity -= v_acc*sec;
				alt -= sec*vertical_velocity;


				myWriter.write(""+df2.format(time)+
						","+df2.format(vertical_velocity)+
						","+df2.format(horizontal_velocity)+
						","+df2.format(dist)+
						","+df2.format(+alt)+
						","+df2.format(ang)+
						","+df2.format(weight)+
						","+df2.format(acc)+
						","+df2.format(fuel)+
						","+df2.format(NN)+"\n");

				if(time%10==0 || alt<100) {
					System.out.println("time="+df2.format(time)+
							",\tvv="+df2.format(vertical_velocity)+
							",\thv="+df2.format(horizontal_velocity)+
							",\tdist="+df2.format(dist)+
							",\talt="+df2.format(+alt)+
							",\tang="+df2.format(ang)+
							",\tweight="+df2.format(weight)+
							",\tacc="+df2.format(acc)+
							",\tfuel="+df2.format(fuel)+
							",\tNN="+df2.format(NN));
				}
			}
				myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

}