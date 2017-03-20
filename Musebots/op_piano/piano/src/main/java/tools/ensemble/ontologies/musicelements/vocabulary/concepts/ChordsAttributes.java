package tools.ensemble.ontologies.musicelements.vocabulary.concepts;

/**
 * Created by OscarAlfonso on 3/20/2017.
 */
public class ChordsAttributes {
        private int rootPitch;
        private String majorOrMinor;
        private int extension;

        public void setRootPitch(int root)
        {
            this.rootPitch = root;
        }
        public int getRootPitch()
        {
            return rootPitch;
        }
        public void setMajorOrMinor(String majorOrMinor)
        {
            this.majorOrMinor = majorOrMinor;
        }
        public String getMajorOrMinor()
        {
            return majorOrMinor;
        }
        public void setExtension(int extension)
        {
            this.extension = extension;
        }
        public int getExtension()
        {
            return this.extension;
        }
    

}
