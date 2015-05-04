package org.swrlapi.core;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgumentFactory;
import org.swrlapi.builtins.arguments.impl.DefaultSWRLBuiltInArgumentFactory;
import org.swrlapi.core.impl.DefaultOWLLiteralFactory;
import org.swrlapi.core.impl.DefaultLiteralFactory;
import org.swrlapi.core.impl.DefaultSWRLAPIOWLDataFactory;
import org.swrlapi.core.impl.DefaultOWLDatatypeFactory;
import org.swrlapi.core.impl.DefaultSWRLAPIOWLOntology;
import org.swrlapi.core.impl.DefaultSWRLAPIOntologyProcessor;
import org.swrlapi.core.impl.DefaultSWRLRuleAndQueryEngineFactory;
import org.swrlapi.exceptions.SWRLAPIException;
import org.swrlapi.exceptions.SWRLRuleEngineException;
import org.swrlapi.sqwrl.DefaultSQWRLResult;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResultGenerator;
import org.swrlapi.sqwrl.values.SQWRLResultValueFactory;
import org.swrlapi.sqwrl.values.impl.DefaultSQWRLResultValueFactory;
import org.swrlapi.ui.controller.DefaultSWRLRuleEngineController;
import org.swrlapi.ui.controller.SWRLRuleEngineController;
import org.swrlapi.ui.dialog.DefaultSWRLAPIDialogManager;
import org.swrlapi.ui.dialog.SWRLAPIDialogManager;
import org.swrlapi.ui.model.DefaultSQWRLQueryEngineModel;
import org.swrlapi.ui.model.DefaultSWRLRuleEngineModel;
import org.swrlapi.ui.model.SQWRLQueryEngineModel;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.model.SWRLRulesTableModel;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for generating some of the core entities defined by the SWRLAPI.
 *
 * @see org.swrlapi.core.SWRLAPIOWLOntology
 * @see org.swrlapi.core.SWRLAPIOntologyProcessor
 * @see org.swrlapi.core.SWRLRuleAndQueryEngineFactory
 * @see org.swrlapi.sqwrl.values.SQWRLResultValueFactory
 * @see org.swrlapi.core.SWRLAPIOWLDataFactory
 * @see OWLDatatypeFactory
 */
public class SWRLAPIFactory
{
  private static final String SQWRL_ICON_NAME = "SQWRL.gif";
  private static final String OWL2RL_ICON_NAME = "OWL2RL.gif";

  private static final SWRLRuleAndQueryEngineFactory swrlRuleAndQueryEngineFactory = new DefaultSWRLRuleAndQueryEngineFactory();

  public static SWRLRuleEngine createSWRLRuleEngine(OWLOntology ontology) throws SWRLRuleEngineException
  {
    return swrlRuleAndQueryEngineFactory.createSWRLRuleEngine(ontology);
  }

  public static SQWRLQueryEngine createSQWRLQueryEngine(OWLOntology ontology) throws SWRLRuleEngineException
  {
    return swrlRuleAndQueryEngineFactory.createSQWRLQueryEngine(ontology);
  }

  /**
   * Create an empty {@link org.swrlapi.core.SWRLAPIOWLOntology}.
   *
   * @return A SWRLAPI-based wrapper of an OWL ontology
   */
  public static SWRLAPIOWLOntology createSWRLAPIOntology()
  {
    try {
      OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = ontologyManager.createOntology();
      DefaultPrefixManager prefixManager = new DefaultPrefixManager();

      addDefaultPrefixes(ontology, prefixManager);
      addSWRLAPIBuiltInOntologies(ontologyManager);

      return createSWRLAPIOntology(ontology, prefixManager);
    } catch (OWLOntologyCreationException e) {
      throw new SWRLAPIException("Error creating OWL ontology", e);
    }
  }

  /**
   * @param ontology An OWLAPI-based ontology
   * @return A SWRLAPI-based wrapper of an OWL ontology
   */
  public static SWRLAPIOWLOntology createSWRLAPIOntology(OWLOntology ontology)
  {
    DefaultPrefixManager prefixManager = new DefaultPrefixManager();

    addDefaultPrefixes(ontology, prefixManager);
    addSWRLAPIBuiltInOntologies(ontology.getOWLOntologyManager());

    return new DefaultSWRLAPIOWLOntology(ontology, prefixManager);
  }

  /**
   * Create a {@link org.swrlapi.core.SWRLAPIOWLOntology} from an OWLAPI-based
   * {@link org.semanticweb.owlapi.model.OWLOntology}.
   *
   * @param ontology An OWLAPI-based ontology
   * @param prefixManager A prefix manager
   * @return A SWRLAPI-based wrapper of an OWL ontology
   */
  public static SWRLAPIOWLOntology createSWRLAPIOntology(OWLOntology ontology, DefaultPrefixManager prefixManager)
  {
    if (ontology == null)
      throw new SWRLAPIException("supplied OWL ontology is null");

    if (prefixManager == null)
      throw new SWRLAPIException("supplied prefix manager is null");

    addDefaultPrefixes(ontology, prefixManager);
    addSWRLAPIBuiltInOntologies(ontology.getOWLOntologyManager());

    return new DefaultSWRLAPIOWLOntology(ontology, prefixManager);
  }

  /**
   * Create a {@link org.swrlapi.core.SWRLAPIOWLOntology} from a file.
   *
   * @param owlFile A file containing an OWL ontology
   * @return A SWRLAPI-based wrapper of an OWL ontology
   */
  public static SWRLAPIOWLOntology createSWRLAPIOntology(File owlFile)
  {
    if (owlFile == null)
      throw new SWRLAPIException("supplied OWL file is null");

    OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
    OWLOntology ontology = createOWLOntology(ontologyManager, owlFile);

    if (ontology == null)
      throw new SWRLAPIException("failed to create ontology from file " + owlFile.getAbsolutePath());

    DefaultPrefixManager prefixManager = new DefaultPrefixManager();

    addDefaultPrefixes(ontology, prefixManager);
    addSWRLAPIBuiltInOntologies(ontologyManager);

    return createSWRLAPIOntology(ontology, prefixManager);
  }

  /**
   * @param swrlapiOWLOntology A SWRLAPI-based OWL ontology
   * @return A SWRLAPI ontology processor
   */
  public static SWRLAPIOntologyProcessor createSWRLAPIOntologyProcessor(SWRLAPIOWLOntology swrlapiOWLOntology)
  {
    return new DefaultSWRLAPIOntologyProcessor(swrlapiOWLOntology);
  }

  /**
   * @param swrlapiOWLOntology A SWRLAPI-based OWL ontology
   * @return A SWRL built-in argument factory
   */
  public static SWRLBuiltInArgumentFactory createSWRLBuiltInArgumentFactory(SWRLAPIOWLOntology swrlapiOWLOntology)
  {
    return new DefaultSWRLBuiltInArgumentFactory(swrlapiOWLOntology.getIRIResolver());
  }

  /**
   * @param swrlapiOWLOntology A SWRLAPI-based OWL ontology
   * @return A SQWRL result value factory
   */
  public static SQWRLResultValueFactory createSQWRLResultValueFactory(SWRLAPIOWLOntology swrlapiOWLOntology)
  {
    return new DefaultSQWRLResultValueFactory(swrlapiOWLOntology.getIRIResolver());
  }

  /**
   *
   * @return and OWL datatype factory
   */
  public static OWLDatatypeFactory getOWLDatatypeFactory()
  {
    return new DefaultOWLDatatypeFactory();
  }

  /**
   * @param swrlapiOWLOntology A SWRLAPI-based OWL ontology
   * @return A SWRLAPI-based OWL data factory
   */
  public static SWRLAPIOWLDataFactory createSWRLAPIOWLDataFactory(SWRLAPIOWLOntology swrlapiOWLOntology)
  {
    return new DefaultSWRLAPIOWLDataFactory(swrlapiOWLOntology);
  }

  /**
   * @param swrlRuleEngine A SWRL rule engine
   * @param swrlRuleRenderer A SWRL renderer
   * @return A SWRL rule table model
   */
  public static SWRLRulesTableModel createSWRLRulesTableModel(SWRLRuleEngine swrlRuleEngine,
      SWRLRuleRenderer swrlRuleRenderer)
  {
    return new SWRLRulesTableModel(swrlRuleEngine, swrlRuleRenderer);
  }

  /**
   * @return A SWRLAPI-based literal factory
   */
  public static LiteralFactory createSWRLAPILiteralFactory()
  {
    return new DefaultLiteralFactory();
  }

  /**
   * @return A SWRLAPI-based OWL datatype factory
   */
  public static OWLDatatypeFactory createOWLDatatypeFactory()
  {
    return new DefaultOWLDatatypeFactory();
  }

  /**
   * @return An OWL literal factory
   */
  public static OWLLiteralFactory createOWLLiteralFactory()
  {
    return new DefaultOWLLiteralFactory();
  }

  /**
   * @return A SWRL rule engine factory
   */
  public static SWRLRuleAndQueryEngineFactory createSWRLRuleAndQueryEngineFactory()
  {
    return new DefaultSWRLRuleAndQueryEngineFactory();
  }

  public static SQWRLResultGenerator createSQWRLResultGenerator(SQWRLResultValueFactory sqwrlResultValueFactory)
  {
    return new DefaultSQWRLResult(sqwrlResultValueFactory);
  }

  /**
   * @param ruleEngine A SWRL rule engine name
   * @return A SWRL rule engine model
   */
  public static SWRLRuleEngineModel createSWRLRuleEngineModel(SWRLRuleEngine ruleEngine)
  {
    return new DefaultSWRLRuleEngineModel(ruleEngine);
  }

  /**
   * @param queryEngine A SQWRL query engine
   * @return A SQWRL query engine model
   */
  public static SQWRLQueryEngineModel createSQWRLQueryEngineModel(SQWRLQueryEngine queryEngine)
  {
    return new DefaultSQWRLQueryEngineModel(queryEngine);
  }

  /**
   * @param swrlRuleEngineModel A SWRL rule engine model
   * @return A SWRL rule engine controller
   */
  public static SWRLRuleEngineController createSWRLRuleEngineController(SWRLRuleEngineModel swrlRuleEngineModel)
  {
    return new DefaultSWRLRuleEngineController(swrlRuleEngineModel);
  }

  /**
   * @param swrlRuleEngineModel An SWRL rule engine model
   * @return A SWRL rule engine dialog manager
   */
  public static SWRLAPIDialogManager createDialogManager(SWRLRuleEngineModel swrlRuleEngineModel)
  {
    return new DefaultSWRLAPIDialogManager(swrlRuleEngineModel);
  }

  /**
   * @return A SQWRL icon
   * @throws SWRLAPIException If an icon cannot be found
   */
  public static Icon getSQWRLIcon() throws SWRLAPIException
  {
    URL url = SWRLAPIFactory.class.getResource(SQWRL_ICON_NAME);

    if (url != null)
      return new ImageIcon(url);
    else
      throw new SWRLAPIException("No SQWRL icon found!");
  }

  /**
   * @return An icon for an OWL 2 RL reasoner
   * @throws SWRLAPIException If an icon cannot be found
   */
  public static Icon getOWL2RLReasonerIcon() throws SWRLAPIException
  {
    URL url = SWRLAPIFactory.class.getResource(OWL2RL_ICON_NAME);

    if (url != null)
      return new ImageIcon(url);
    else
      throw new SWRLAPIException("No OWL 2 RL icon found!");
  }

  public static OWLLiteralFactory getOWLLiteralFactory()
  {
    return new DefaultOWLLiteralFactory();
  }

  private static void addDefaultPrefixes(OWLOntology ontology, DefaultPrefixManager prefixManager)
  {
    OWLOntologyManager owlOntologyManager = ontology.getOWLOntologyManager();
    OWLDocumentFormat ontologyFormat = owlOntologyManager.getOntologyFormat(ontology);

    if (ontologyFormat.isPrefixOWLOntologyFormat()) {
      PrefixDocumentFormat prefixOntologyFormat = ontologyFormat.asPrefixOWLOntologyFormat();
      String defaultPrefix = prefixOntologyFormat.getDefaultPrefix();

      Map<String, String> map = prefixOntologyFormat.getPrefixName2PrefixMap();
      for (String prefix : map.keySet())
        prefixManager.setPrefix(prefix, map.get(prefix));

      // TODO Look at this. Seems dodgy.
      if (defaultPrefix != null)
        prefixManager.setDefaultPrefix(defaultPrefix);
      else
        prefixManager.setDefaultPrefix(":");
    }
    addSWRLAPIPrefixes(prefixManager);
  }

  private static void addSWRLAPIPrefixes(DefaultPrefixManager prefixManager)
  {
    prefixManager.setPrefix("owl:", "http://www.w3.org/2002/07/owl#");
    prefixManager.setPrefix("swrl:", "http://www.w3.org/2003/11/swrl#");
    prefixManager.setPrefix("swrlb:", "http://www.w3.org/2003/11/swrlb#");
    prefixManager.setPrefix("sqwrl:", "http://sqwrl.stanford.edu/ontologies/built-ins/3.4/sqwrl.owl#");
    prefixManager.setPrefix("swrlm:", "http://swrl.stanford.edu/ontologies/built-ins/3.4/swrlm.owl#");
    prefixManager.setPrefix("temporal:", "http://swrl.stanford.edu/ontologies/built-ins/3.3/temporal.owl#");
    prefixManager.setPrefix("swrlx:", "http://swrl.stanford.edu/ontologies/built-ins/3.3/swrlx.owl#");
    prefixManager.setPrefix("swrla:", "http://swrl.stanford.edu/ontologies/3.3/swrla.owl#");
  }

  private static void addSWRLAPIBuiltInOntologies(OWLOntologyManager ontologyManager)
  {
    Map<String, String> map = new HashMap<>();

    map.put("http://www.w3.org/2003/11/swrl#", resourceName2File("owl/swrl.owl"));
    map.put("http://www.w3.org/2003/11/swrlb#", resourceName2File("owl/swrlb.owl"));
    map.put("http://swrl.stanford.edu/ontologies/3.3/swrla.owl", resourceName2File("owl/swrla.owl"));
    map.put("http://swrl.stanford.edu/ontologies/built-ins/3.4/swrlm.owl", resourceName2File("owl/swrlm.owl"));
    map.put("http://swrl.stanford.edu/ontologies/built-ins/3.3/swrlx.owl", resourceName2File("owl/swrlx.owl"));
    map.put("http://swrl.stanford.edu/ontologies/built-ins/3.3/temporal.owl", resourceName2File("owl/temporal.owl"));
    map.put("http://sqwrl.stanford.edu/ontologies/built-ins/3.4/sqwrl.owl", resourceName2File("owl/sqwrl.owl"));

    for (String key : map.keySet())
      ontologyManager.getIRIMappers().add(new SimpleIRIMapper(IRI.create(key), IRI.create(map.get(key))));
  }

  private static OWLOntology createOWLOntology(OWLOntologyManager ontologyManager, File file) throws SWRLAPIException
  {
    try {
      return ontologyManager.loadOntologyFromOntologyDocument(file);
    } catch (OWLOntologyCreationException e) {
      throw new SWRLAPIException("Error create OWL ontology from file " + file.getAbsolutePath() + ": "
          + e.getMessage());
    }
  }

  // TODO This looks dodgy
  private static String resourceName2File(String resourceName)
  {
    URL url = SWRLAPIFactory.class.getClassLoader().getResource(resourceName);
    if (url == null)
      throw new SWRLAPIException("Could not find resource " + resourceName);
    return "file:///" + url.getFile();
  }
}
