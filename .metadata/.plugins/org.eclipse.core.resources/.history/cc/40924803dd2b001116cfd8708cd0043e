package com.kokakiwi.mclauncher.utils.java;

public class Version implements Comparable<Version>
{
    private final int[] nums;
    
    public Version(int... is)
    {
        nums = is;
    }
    
    public int[] getVersionNumbers()
    {
        return nums;
    }
    
    public static Version parseString(String version)
    {
        final String[] splitted = version.split("\\.");
        final int[] nums = new int[splitted.length];
        for (int i = 0; i < splitted.length; i++)
        {
            nums[i] = Integer.parseInt(splitted[i]);
        }
        
        return new Version(nums);
    }
    
    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < nums.length; i++)
        {
            sb.append(String.valueOf(nums[i]));
            if (i < nums.length - 1)
            {
                sb.append('.');
            }
        }
        
        return sb.toString();
    }
    
    public int compareTo(Version arg)
    {
        final int minNums = Math.min(nums.length,
                arg.getVersionNumbers().length);
        
        int diff = 0;
        for (int i = 0; i < minNums; i++)
        {
            if (nums[i] > arg.getVersionNumbers()[i])
            {
                diff++;
                break;
            }
            else if (nums[i] < arg.getVersionNumbers()[i])
            {
                diff--;
                break;
            }
            else
            {
                continue;
            }
        }
        
        if (diff == 0)
        {
            if (nums.length > arg.getVersionNumbers().length)
            {
                diff++;
            }
            else if (nums.length < arg.getVersionNumbers().length)
            {
                diff--;
            }
        }
        
        return diff;
    }
    
}
