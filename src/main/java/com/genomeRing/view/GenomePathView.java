package com.genomeRing.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import com.genomeRing.model.structure.CoveredBlock;
import com.genomeRing.model.structure.Genome;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.view.genomeRingWindow.GenomeRingWindow;

import java.util.ArrayList;
import java.util.List;


/**
 * Draws the Path for one Genome.
 * The cost calculation functions are kept in although they are not needed.
 */
@SuppressWarnings("serial")
public class GenomePathView extends Path {


    protected static boolean ONLY_CLOCKWISE = false;
    public final static int CONNECTOR_ALPHA = 24;

    // compute the "price" of this path
    protected double cost_angles;
    protected int cost_blocks_jumped;
    protected int cost_jumps;

    public GenomePathView(Genome g, RingDimensions ringdim, GenomeRingWindow window) {

        this.visibleProperty().bind(window.getController().getShowPathsCheckBox().selectedProperty());
        g.colorProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) {
                if (newValue.equals(javafx.scene.paint.Color.TRANSPARENT)) {
                    setStroke(Color.TRANSPARENT);
                } else {
                    setStroke(GenomeColors.alphaColor(newValue, CONNECTOR_ALPHA));
                }
            }
        });


        int gidx = g.getIndex();
        List<CoveredBlock> blocks = g.getBlocks(); // blocks sorted in order of genome

        // find consecutive blocks with same direction (clockwise, counterclockwise) to maximize number of direct connections

        List<ConsecutiveBlocks> groups = new ArrayList<ConsecutiveBlocks>();
        ConsecutiveBlocks group = new ConsecutiveBlocks(ringdim);
        groups.add(group);

        for (int i = 0; i != blocks.size(); ++i) {
            CoveredBlock b = blocks.get(i);
            if (!group.add(b)) {
                groups.add(group = new ConsecutiveBlocks(ringdim));
                group.add(b);
            }
        }
//		
//		System.out.println(g.getIndex());
//		for (ConsecutiveBlocks cb : groups)
//			System.out.println(cb);

        // now, when drawing blocks, keep direction as long as possible.
        // single blocks with direction==0 use direction of previous consecutive blocks

        java.util.Iterator<ConsecutiveBlocks> groupIter = groups.iterator();
        group = groupIter.next(); // there is at least one group
        int groupOffset = 0;
        int currentDirection = group.getDirection();
        int lastDirection = currentDirection;

        for (int blockIndex = 0; blockIndex != blocks.size() - 1; ++blockIndex) {
            CoveredBlock b = blocks.get(blockIndex);
            CoveredBlock nextb = blocks.get(blockIndex + 1);


            lastDirection = currentDirection;

            ++groupOffset;
            if (groupOffset == group.size()) {
                group = groupIter.next(); // check to see if direction needs change
                if (group.getDirection() != 0) {
                    currentDirection = group.getDirection();
                } else if (!ONLY_CLOCKWISE) {
                    // no preference, use shorter angle
                    double alpha_end = lastDirection == 1 ? ringdim.getEndDegree(b) : ringdim.getStartDegree(b);
                    double alpha_next_start = currentDirection == 1 ? ringdim.getStartDegree(nextb) : ringdim.getEndDegree(nextb);
                    double alpha_dist = (alpha_end - alpha_next_start);
                    alpha_dist += 3600;
                    alpha_dist %= 360;
                    currentDirection = alpha_dist < 180 ? 1 : -1;

                    if (lastDirection == 0 && blockIndex == 0) {
                        // use best fit for first block if no direction given
                        lastDirection = currentDirection;
                    }

                }
                groupOffset = 0;
            }

            if (lastDirection == 0)
                lastDirection = 1;

            addBlock(b, ringdim, gidx, lastDirection, blockIndex != 0);

            // add block connection.
            addConnector(b, nextb, ringdim, gidx, lastDirection, currentDirection);

            // store info for flags
            b.setDrawnClockwise(lastDirection == 1);

        }
        // add last block (jump is already there)
        CoveredBlock lastBlock = blocks.get(blocks.size() - 1);
        addBlock(lastBlock, ringdim, gidx, currentDirection, true);

        lastBlock.setDrawnClockwise(currentDirection == 1);

        // if genome is circular: add final jump
        if (g.isCircular()) {
            if (ONLY_CLOCKWISE) {
                currentDirection = 1;
            } else {
                groupIter = groups.iterator();
                while (groupIter.hasNext() && ((currentDirection = groupIter.next().getDirection()) == 0)) ;
                if (currentDirection == 0)
                    currentDirection = 1;
            }
            addConnector(lastBlock, blocks.get(0), ringdim, gidx, lastDirection, currentDirection);
            //closePath();
           // this.getElements().add(new ClosePath());
        }

//		System.out.println("Genome "+g.getName()+"\tJumps="+cost_jumps+"\tBlocks="+cost_blocks_jumped+"\tAngles="+cost_angles);
    }


    public double[] getCosts() {
        return new double[]{cost_jumps, cost_blocks_jumped, cost_angles};
    }

    protected void addJumpCost(int blocks, double angle) {
        cost_jumps++;
        cost_blocks_jumped += blocks;
        cost_angles += angle;
//		System.out.println("Added block cost "+blocks+" \tAdded angular cost "+angle);
    }


    /**
     * Draws the path elements equal to genome segments.
     *
     * @param b
     * @param ringdim
     * @param gidx
     * @param direction
     * @param connect
     */
    private void addBlock(CoveredBlock b, RingDimensions ringdim, int gidx, int direction, boolean connect) {
        //System.out.println("Drawing Block with direction "+direction+": "+b);

        double segment_radius = b.isForward() ? ringdim.getRadiusForward(gidx) : ringdim.getRadiusBackward(gidx);
        double alpha_start = ringdim.getStartDegree(b);
        double alpha_end = ringdim.getEndDegree(b);

        addSegment(segment_radius, alpha_start, alpha_end, direction, connect, b);

    }

    /**
     * Adds a connecting PathElement depending on the connecting needed. This can either be a normal segment,
     * if they are neighbors otherwise it needs either a outer jump, inner jump or an interchanging jump.
     *
     * @param b             current CoveredBlock
     * @param nextb         next CoveredBlock
     * @param ringdim
     * @param gidx
     * @param lastDirection
     * @param direction
     */
    private void addConnector(CoveredBlock b, CoveredBlock nextb, RingDimensions ringdim, int gidx, int lastDirection, int direction) {
//		System.out.println("Connecting Block with directions "+lastDirection+" -> "+direction+": "+nextb);

        double segment_radius = b.isForward() ? ringdim.getRadiusForward(gidx) : ringdim.getRadiusBackward(gidx);

        int NoB = ringdim.getNumberOfBlocks();
        // -1 % k == -1  UND NICHT: k-1
        boolean neighbours = ((NoB + b.getIndex() + direction) % NoB) == nextb.getIndex();
        neighbours &= lastDirection == direction;

        double alpha_end = lastDirection == 1 ? ringdim.getEndDegree(b) : ringdim.getStartDegree(b);
        double alpha_next_start = direction == 1 ? ringdim.getStartDegree(nextb) : ringdim.getEndDegree(nextb);

        if (direction == 1) {
            while (alpha_end < alpha_next_start)
                alpha_end += 360;
        } else {
            while (alpha_next_start < alpha_end)
                alpha_next_start += 360;
        }


        // cost computation
        double angle = Math.abs(alpha_next_start - alpha_end);
        if (angle > 180 && !ONLY_CLOCKWISE)
            angle = 360 - angle; // reverse direction
        int blocks_jumped = nextb.getIndex() - b.getIndex();
        if (blocks_jumped < 0)
            blocks_jumped += ringdim.getNumberOfBlocks(); // wrap around
        blocks_jumped--; // neighbor is free

        // five cases: direct same, direct switch, jump inner, jump outer, jump between
        if (neighbours) { // direct connection
            if (b.isForward() == nextb.isForward()) { // stay on same circle --COST:none
                addSegment(segment_radius, alpha_end, alpha_next_start, direction, true, b);
            } else { // switch circles directly
                // nothing to do, will be automatically connected to next segment by path rendering
                //--COST:none in terms of angles, one in terms of jump
                addJumpCost(0, 0);
            }
        } else { // jump needed: outer / exchange / inner
            if (b.isForward() && nextb.isForward()) { // outer jump
//				System.out.println("Block "+b.getBlock().getName()+" to "+nextb.getBlock().getName());
                int turn = addOuterJump(gidx,
                        segment_radius,
                        alpha_end, alpha_next_start,
                        ringdim.getBlockGap(),
                        lastDirection,
                        direction,
                        ringdim, b);
//				System.out.print(gidx+"outer "+turn+"    \t");				
                if (turn == -1)
                    blocks_jumped = ringdim.getNumberOfBlocks() - blocks_jumped - 2;
                addJumpCost(blocks_jumped, angle);
            } else if (b.isForward() != nextb.isForward()) { // exchange jump
                double asrc = alpha_end - lastDirection * ringdim.getBlockGap() * 0.75;
                double atgt = alpha_next_start + direction * ringdim.getBlockGap() * 0.75;
                int turn = addInterchangeJump(gidx, ringdim, asrc, atgt, b, nextb, direction);
                // interchange jumps can go backwards.
//				System.out.print(gidx+"interchange"+turn+"\t");
                if (turn == -1)
                    blocks_jumped = ringdim.getNumberOfBlocks() - blocks_jumped - 2;
                addJumpCost(blocks_jumped, angle);
            } else { // inner jump
                addInnerJump(segment_radius, ringdim.getRadiusInnerJump(gidx), alpha_end, alpha_next_start);
                // inner jump always is a direct jump, i.e. always cheapest
//				System.out.print(gidx+"inner         \t");
                addJumpCost(blocks_jumped, angle);
            }
        }
    }


    /**
     * @return direction of the jump
     */
    private int addOuterJump(
            int gidx, double radius_segment,
            double alpha_start, double alpha_end, double gap,
            int lastDirection, int direction,
            RingDimensions rd, CoveredBlock block) {
        if (!ONLY_CLOCKWISE) {
            double as = alpha_start;
            double ae = alpha_end;
            while (as < 0 || ae < 0) {
                as += 360;
                ae += 360;
            }

            if (as - ae > 180) {
                ae += 360;
            } else if (as - ae < -180) {
                as += 360;
            }

            alpha_start = as;
            alpha_end = ae;
        }

        int turn = alpha_start > alpha_end ? 1 : -1;

        double arc_starting_on_angle = alpha_start - turn * gap / 2;
        double arc_ending_on_angle = alpha_end + turn * gap / 2;

        // ---- OLD JUMPLEVEL CODE ----
//		if (Math.abs(arc_ending_on_angle-arc_starting_on_angle)<=90) {
//		radius_jumplevel = rd.getRadiusOuterJump(g.getIndex(), 1);
//	}
//		double outergap = gap * (1-((double)gidx/(double)rd.getNumberOfGenomes()));
//		gap=outergap;
        double radius_jumplevel = rd.getRadiusOuterJump(gidx, (int) arc_ending_on_angle, (int) arc_starting_on_angle);

        double bezier1X = polarToX(radius_segment, alpha_start - lastDirection * gap / 4);
        double bezier1Y = polarToY(radius_segment, alpha_start - lastDirection * gap / 4);
        double bezier2X = polarToX(radius_jumplevel, alpha_start - lastDirection * gap / 4);
        double bezier2Y = polarToY(radius_jumplevel, alpha_start - lastDirection * gap / 4);
        double targetX = polarToX(radius_jumplevel, arc_starting_on_angle);
        double targetY = polarToY(radius_jumplevel, arc_starting_on_angle);
        //curveTo(bezier1X, bezier1Y, bezier2X, bezier2Y, targetX, targetY);
        this.getElements().add(new CubicCurveTo(bezier1X, bezier1Y, bezier2X, bezier2Y, targetX, targetY));

       // double startX = polarToX(radius_jumplevel, arc_starting_on_angle);
      //  double startY = polarToY(radius_jumplevel, arc_starting_on_angle);

        double endX = polarToX(radius_jumplevel, arc_ending_on_angle);
        double endY = polarToY(radius_jumplevel, arc_ending_on_angle);

        double length = Math.abs(arc_ending_on_angle - arc_starting_on_angle);
        //System.out.println(turn + " " + length + " "+ lastDirection + " " + direction);

        //different directions or "turns" need different parameters for the ArcTo() Constructor
        if(lastDirection == direction && lastDirection == -1)
            if (turn == 1) {
                ArcTo arcTo = new ArcTo(radius_jumplevel, radius_jumplevel, length, endX, endY, false, true);
                this.getElements().add(arcTo);
            } else {
                //correct
                ArcTo arcTo = new ArcTo(radius_jumplevel, radius_jumplevel, length, endX, endY, false, false);
                this.getElements().add(arcTo);
            }
        else if(lastDirection == direction && lastDirection == 1){
            if (turn == 1) {
                ArcTo arcTo = new ArcTo(radius_jumplevel, radius_jumplevel, length, endX, endY, false, true);
                this.getElements().add(arcTo);
            } else {
                ArcTo arcTo = new ArcTo(radius_jumplevel, radius_jumplevel, length, endX, endY, false, false);
                this.getElements().add(arcTo);
            }
        }
        else if(lastDirection == 1 && direction == -1){
            if (turn == 1) {
                ArcTo arcTo = new ArcTo(radius_jumplevel, radius_jumplevel, length, endX, endY, false, true);
                this.getElements().add(arcTo);
            } else {
                ArcTo arcTo = new ArcTo(radius_jumplevel, radius_jumplevel, length, endX, endY, false, false);
                this.getElements().add(arcTo);
            }

        }else if(lastDirection == -1 && direction == 1){
            if (turn == 1) {
                ArcTo arcTo = new ArcTo(radius_jumplevel, radius_jumplevel, length, endX, endY, false, true);
                this.getElements().add(arcTo);
            } else {
                ArcTo arcTo = new ArcTo(radius_jumplevel, radius_jumplevel, length, endX, endY, false, false);
                this.getElements().add(arcTo);
            }
        }


       // addSegment(radius_jumplevel, arc_starting_on_angle, arc_ending_on_angle, turn, true, block);

        bezier1X = polarToX(radius_jumplevel, alpha_end + direction * gap / 4);
        bezier1Y = polarToY(radius_jumplevel, alpha_end + direction * gap / 4);
        bezier2X = polarToX(radius_segment, alpha_end + direction * gap / 4);
        bezier2Y = polarToY(radius_segment, alpha_end + direction * gap / 4);
        targetX = polarToX(radius_segment, alpha_end);
        targetY = polarToY(radius_segment, alpha_end);

        //curveTo(bezier1X, bezier1Y, bezier2X, bezier2Y, targetX, targetY);
        this.getElements().add(new CubicCurveTo(bezier1X, bezier1Y, bezier2X, bezier2Y, targetX, targetY));
        return turn;
    }

    private void addInnerJump(double radius_segment, double radius_jumplevel, double alpha_start, double alpha_end) {
        double adiff = alpha_end - alpha_start;
        double targetX = polarToX(radius_segment, alpha_end);
        double targetY = polarToY(radius_segment, alpha_end);
        double bezier1X = polarToX(radius_jumplevel, alpha_start + .10 * adiff);
        double bezier1Y = polarToY(radius_jumplevel, alpha_start + .10 * adiff);
        double bezier2X = polarToX(radius_jumplevel, alpha_end - .10 * adiff);
        double bezier2Y = polarToY(radius_jumplevel, alpha_end - .10 * adiff);


        if (this.getElements().isEmpty()) {
            this.getElements().add(new MoveTo(0, 0));
        }
        this.getElements().add(new CubicCurveTo(bezier1X, bezier1Y, bezier2X, bezier2Y, targetX, targetY));
        //curveTo(bezier1X, bezier1Y, bezier2X, bezier2Y, targetX, targetY);
    }

    /**
     * @return direction of the jump
     */
    private int addInterchangeJump(int gidx, RingDimensions ringdim, double alpha_start, double alpha_end, CoveredBlock b, CoveredBlock nextb, int direction) {
        // draw segment on interchange level
        int turn = 1;
        if (!ONLY_CLOCKWISE) {
            double as = alpha_start;
            double ae = alpha_end;
            while (as < 0 || ae < 0) {
                as += 360;
                ae += 360;
            }

            if (as - ae > 180) {
                ae += 360;
                turn = -1;
            } else if (as - ae < -180) {
                as += 360;
                turn = -1;
            }

            alpha_start = as;
            alpha_end = ae;
        }

        // Calculating parameters for the interchange segment
        double radius_interchange = ringdim.getRadiusInterchangeJump(gidx, (int) alpha_end, (int) alpha_start);
        double startX = polarToX(radius_interchange, alpha_start);
        double startY = polarToY(radius_interchange, alpha_start);

        double endX = polarToX(radius_interchange, alpha_end);
        double endY = polarToY(radius_interchange, alpha_end);

        double length = Math.abs(alpha_end - alpha_start);
		//System.out.println(turn + " " + length + " "+ b.isForward()  +" " + nextb.isForward()+ "" + radius_interchange + " " + direction);

        //different directions or "turns" need different parameters for the ArcTo() Constructor
		if(direction == -1)
			if (turn == -1) {
				this.getElements().add(new LineTo(endX, endY));
				ArcTo arcTo = new ArcTo(radius_interchange, radius_interchange, length, startX, startY, false, false);
				this.getElements().add(arcTo);
			} else {
				this.getElements().add(new LineTo(startX, startY));
				ArcTo arcTo = new ArcTo(radius_interchange, radius_interchange, length, endX, endY, false, false);
				this.getElements().add(arcTo);
			}
		else{
			if (turn == -1) {
				this.getElements().add(new LineTo(endX, endY));
				ArcTo arcTo = new ArcTo(radius_interchange, radius_interchange, length, startX, startY, false, false);
				this.getElements().add(arcTo);
			} else {
				this.getElements().add(new LineTo(startX, startY));
				ArcTo arcTo = new ArcTo(radius_interchange, radius_interchange, length, endX, endY, false, true);
				this.getElements().add(arcTo);
			}
		}

        return turn;
    }

    /**
     * Adding a standard arc. We have to consider direction and strand.
     * If it should not be connected we just move to the starting point of the arc.
     *
     * @param alpha_start
     * @param alpha_end
     * @param connect
     */
    private void addSegment(double radius, double alpha_start, double alpha_end, int direction, boolean connect, CoveredBlock block) {

        double startX = polarToX(radius, alpha_start);
        double startY = polarToY(radius, alpha_start);

        double endX = polarToX(radius, alpha_end);
        double endY = polarToY(radius, alpha_end);

        double length = Math.abs(alpha_end - alpha_start);

       // System.out.println(length);

        //different directions or "turns" need different parameters for the ArcTo() Constructor
        if (direction == -1) {
            if (!connect) {
                this.getElements().add(new MoveTo(endX, endY));
            }
            this.getElements().add(new LineTo(endX, endY));
            if (length >= 180) {
                ArcTo arcTo = new ArcTo(radius, radius, length, startX, startY, true, false);
                this.getElements().add(arcTo);
            } else {
                ArcTo arcTo = new ArcTo(radius, radius, length, startX, startY, false, false);
                this.getElements().add(arcTo);
            }
        } else {
            if (!connect) {
                this.getElements().add(new MoveTo(startX, startY));
            }
            this.getElements().add(new LineTo(startX, startY));
            if (length >= 180) {
                ArcTo arcTo = new ArcTo(radius, radius, length, endX, endY, true, true);
                this.getElements().add(arcTo);
            } else {
                ArcTo arcTo = new ArcTo(radius, radius, length, endX, endY, false, true);
                this.getElements().add(arcTo);
            }

        }
    }


    protected double polarToX(double radius, double alpha) {
        return Math.sin(Math.toRadians(90 + alpha)) * radius;
    }

    protected double polarToY(double radius, double alpha) {
        return Math.cos(Math.toRadians(90 + alpha)) * radius;
    }


    private class ConsecutiveBlocks extends ArrayList<CoveredBlock> {
        int lastIndex = -1;
        int direction = 0;
        RingDimensions rd;

        public ConsecutiveBlocks(RingDimensions rd) {
            this.rd = rd;
        }

        public boolean add(CoveredBlock b) {
            if (lastIndex != -1) {

                if (get(size() - 1).isForward() != b.isForward())
                    return false;

                int indexDifference = b.getBlock().getIndex() - lastIndex;
                // special case: one block is at end of supergenome, one at start
                if (Math.abs(indexDifference) == rd.getNumberOfBlocks() - 1) {
                    indexDifference = -(int) Math.signum(indexDifference);
                }

                // first check if this block is in fact consecutive to the existing ones
                if (Math.abs(indexDifference) > 1) {
                    // it is not
                    return false;
                }
                if (direction == 0) { // establish direction for the first time
                    direction = indexDifference;
                } else {
                    if (direction != indexDifference) { // consecutive, but change of direction
                        throw new RuntimeException("Direction changes within consecutive blocks! One block may be added twice\n" + toString());
                    }
                }
            }
            super.add(b);
            lastIndex = b.getIndex();
            return true;
        }

        public int getDirection() {
            if (ONLY_CLOCKWISE)
                return 1;
            return direction;
        }

        public String toString() {
            return (direction == 1 ? "CW " : (direction == -1 ? "CCW" : "---")) + ":" + super.toString();
        }
    }

}
