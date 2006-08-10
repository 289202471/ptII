package ptolemy.actor.ptalon;

import java.util.Hashtable;
import java.util.Stack;

import com.microstar.xml.HandlerBase;
import com.microstar.xml.XmlParser;

public class PtalonMLHandler extends HandlerBase {
    
    /**
     * Create a PtalonMLHandler, which will be used to recover the
     * AST and code manager specified in the PtalonML.
     * @param actor The actor to associate with this handler.
     */
    public PtalonMLHandler(PtalonActor actor) {
        super();
        _attributes = new Hashtable<String, String>();
        _actor = actor;
    }   
    
    /**
     * Process a PtalonML attribute.
     * @param aname The name of the attribute.
     * @param value The value of the attribute, or null if the attribute
     *        is <code>#IMPLIED</code>.
     * @param isSpecified True if the value was specified, false if it
     *       was defaulted from the DTD.
     * @exception java.lang.Exception If there is any trouble creating
     * the AST or code manager,
     */
    public void attribute(String aname, String value, boolean isSpecified) throws Exception {
        if ((aname != null) && (value != null)) {
            _attributes.put(aname, value);
        }
    }

    /**
     * Process the end of a PtalonML element.
     * @param elname The element type name.
     * @exception java.lang.Exception If there is any trouble creating
     * the AST or code manager.
     */
    public void endElement(String elname) throws Exception {
        if (elname.equals("ptolemy.actor.ptalon.PtalonAST")) {
            if (_astStack.size() == 1) {
                _ast = _astStack.pop();
            } else {
                _astStack.pop();
                _ast = (PtalonAST) _astStack.peek();                
            }
        } else if (elname.equals("if")) {
            if (_manager != null) {
                try {
                    _manager.popIfStatement();
                } catch (PtalonScopeException e) {
                }
            }
        }
    }
    
    /**
     * @return The AST generated by the PtalonML code.
     */
    public PtalonAST getAST() {
        return _ast;
    }
    
    /**
     * @return The CodeManager generated by the PtalonML code.
     */
    public CodeManager getCodeManager() {
        return _manager;
    }
    
    /**
     * Process the start of a PtalonML element.
     * @param elname The element type name.
     * @exception java.lang.Exception If there is any trouble creating
     * the AST or code manager,
     */
    public void startElement(String elname) throws Exception {
        if (elname.equals("ptolemy.actor.ptalon.PtalonAST")) {
            if (_ast == null) {
                _ast = new PtalonAST();
            } else {
                PtalonAST child = new PtalonAST();
                _ast.addChild(child);
                _ast = child;
            }
            for (String name : _attributes.keySet()) {
                if (name.equals("text")) {
                    _ast.setText(_attributes.get(name));
                } else if (name.equals("type")) {
                    _ast.setType(new Integer(_attributes.get(name)));
                }
            }
            _astStack.push(_ast);
        } else if (elname.equals("if")) {
            if (_manager == null) {
                _manager = new CodeManager(_actor);
            }
            if (_attributes.containsKey("name")) {
                _manager.pushIfStatement(_attributes.get("name"));
            }
        } else if (elname.equals("symbol")) {
            if (_attributes.containsKey("name") && _attributes.containsKey("type")
                    && _attributes.containsKey("status")
                    && _attributes.containsKey("uniqueName")) {
                String name = _attributes.get("name");
                String type = _attributes.get("type");
                boolean status = new Boolean(_attributes.get("status"));
                String uniqueName = _attributes.get("uniqueName");
                _manager.addSymbol(name, type, status, uniqueName);
            }
        }
        _attributes.clear();
    }
    
    /**
     * The actor that created this handler.
     */
    PtalonActor _actor;
    
    /**
     * The AST created by this handler.
     */
    PtalonAST _ast;
    
    /**
     * A temporary node that gets updated to populate the AST.
     */
    PtalonAST _astNode;
    
    /**
     * A stack which stores the ancestors of _astNode;
     */
    Stack<PtalonAST> _astStack = new Stack<PtalonAST>();
    
    /**
     * Each element in this hashtable maps a name to a value.
     */
    Hashtable<String, String> _attributes;
    
    /**
     * The code created by this handler.
     */
    CodeManager _manager;


    

}
