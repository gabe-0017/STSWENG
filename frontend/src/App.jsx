import React, { useEffect, useRef, useState } from "react";
import {
  ArrowRight,
  BadgeCheck,
  Building2,
  CheckCircle2,
  ChevronDown,
  ClipboardList,
  FileCheck2,
  HeartHandshake,
  Home,
  LockKeyhole,
  LogIn,
  Menu,
  QrCode,
  ShieldCheck,
  Store,
  Users,
  WalletCards,
  X,
} from "lucide-react";
import { api, saveSession, clearSession, getSession } from "./api";

/* ==========================================================================
   Data
   ========================================================================== */

const roles = [
  {
    id: "citizen",
    label: "Citizen",
    icon: Users,
    description: "Manage your Senior Citizen and PWD registrations and digital IDs.",
  },
  {
    id: "lgu",
    label: "LGU / OSCA Staff",
    icon: Building2,
    description: "Review and verify SC and PWD applications.",
  },
  {
    id: "establishment",
    label: "Establishment",
    icon: Store,
    description: "Verify SC and PWD IDs and record eligible benefits.",
  },
  {
    id: "government",
    label: "Government Agency",
    icon: ShieldCheck,
    description: "Monitor registry activity and benefit claims.",
  },
];

const ROLE_MAP = {
  // One CITIZEN role covers both Senior Citizen and PWD registrants. The dashboard view
  // (senior vs PWD) will later be chosen from the registration type, not from the role.
  CITIZEN: "citizen",
  LGU_STAFF: "lgu",
  ESTABLISHMENT: "establishment",
  GOV_ADMIN: "government",
};

/* ==========================================================================
   App shell
   ========================================================================== */

/* Pages live in the URL hash (#/login, #/register, #/dashboard) so the browser's
   Back and Forward buttons work. The server never sees the hash, so no server
   routing is needed. */
const PAGES = ["home", "login", "register", "dashboard"];

function pageFromHash() {
  const name = window.location.hash.replace(/^#\/?/, "");
  return PAGES.includes(name) ? name : "home";
}

function App() {
  const [routePage, setRoutePage] = useState(pageFromHash);
  // The dashboard needs a login: without a session, show the login page instead.
  const page = routePage === "dashboard" && !getSession() ? "login" : routePage;
  const pendingSection = useRef(null);

  function setPage(next) {
    const hash = next === "home" ? "#/" : `#/${next}`;
    if (window.location.hash === hash || (next === "home" && !window.location.hash)) {
      setRoutePage(next);
    } else {
      window.location.hash = hash;
    }
  }

  useEffect(() => {
    const onHashChange = () => setRoutePage(pageFromHash());
    window.addEventListener("hashchange", onHashChange);
    return () => window.removeEventListener("hashchange", onHashChange);
  }, []);

  // Every page change starts at the top, unless a section link asked for a section.
  useEffect(() => {
    const section = pendingSection.current;
    pendingSection.current = null;
    if (section) {
      document.getElementById(section)?.scrollIntoView();
    } else {
      window.scrollTo(0, 0);
    }
  }, [page]);

  function goHome() {
    setMobileMenu(false);
    if (page === "home") window.scrollTo({ top: 0, behavior: "smooth" });
    else setPage("home");
  }

  function goSection(id) {
    setMobileMenu(false);
    if (page === "home") {
      document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
    } else {
      pendingSection.current = id;
      setPage("home");
    }
  }

  const [role, setRole] = useState(() => ROLE_MAP[getSession()?.role] ?? "citizen");
  const [mobileMenu, setMobileMenu] = useState(false);
  const [notice, setNotice] = useState("");

  function openLogin(selectedRole = role) {
    setRole(selectedRole);
    setNotice("");
    setPage("login");
    setMobileMenu(false);
  }

  async function handleLogin(email, password) {
    const session = await api("/auth/login", {
      method: "POST", body: { email, password }, auth: false,
    });
    saveSession(session);
    setRole(ROLE_MAP[session.role]);
    setPage("dashboard");
  }

  function logout() {
    clearSession();
    setPage("home");
  }

  useEffect(() => {
    window.addEventListener("ccid:logout", logout);
    return () => window.removeEventListener("ccid:logout", logout);
  }, []);

  return (
    <div className="app-shell">
      {page !== "dashboard" && (
      <header className="topbar">
        <button
          className="brand"
          onClick={goHome}
          aria-label="CivicCareID home"
        >
          <span className="brand-mark logo-text">CC</span>
          <span>
            <strong>CivicCareID</strong>
            <small>PHILIPPINES</small>
          </span>
        </button>

        <button
          className="mobile-toggle"
          onClick={() => setMobileMenu(!mobileMenu)}
          aria-label="Toggle menu"
        >
          {mobileMenu ? <X /> : <Menu />}
        </button>

        <nav className={mobileMenu ? "nav-links open" : "nav-links"}>
          <button onClick={goHome}>Home</button>
          <button onClick={() => goSection("how-it-works")}>How it works</button>
          <button onClick={() => goSection("about")}>About</button>
          <button
            className="nav-signup"
            onClick={() => { setMobileMenu(false); setPage("register"); }}
          >
            Sign up
          </button>
          <button
            className="nav-login"
            onClick={() => { setMobileMenu(false); openLogin(); }}
          >
            Log in <ArrowRight size={16} />
          </button>
        </nav>
      </header>
      )}

      {page === "home" && (
        <HomePage openLogin={openLogin} setPage={setPage} goSection={goSection} />
      )}
      {page === "login" && (
        <LoginPage
          handleLogin={handleLogin}
          setPage={setPage}
        />
      )}
      {page === "register" && (
        <RegisterPage
          setPage={setPage}
          onRegistered={() => {
            setRole("citizen");
            setPage("dashboard");
          }}
        />
      )}
      {page === "dashboard" && (
        <Dashboard
          role={role}
          setPage={setPage}
          logout={logout}
          notice={notice}
          setNotice={setNotice}
        />
      )}

      <footer className="footer">
        <div className="footer-brand">
          <span className="brand-mark small logo-text">CC</span>
          <strong>CivicCareID PH</strong>
        </div>
        
        <span>© 2026 CivicCareID PH · Group 019 · Academic prototype, all data is synthetic</span>
      </footer>
    </div>
  );
}

/* ==========================================================================
   Home page
   ========================================================================== */

const portals = [
  {
    key: "citizen",
    icon: Users,
    title: "Senior Citizens & PWD",
    text: "Apply for your Senior Citizen or PWD ID and keep your digital ID with you.",
    link: "Register or log in",
    target: "register",
  },
  {
    key: "lgu",
    icon: Building2,
    title: "LGU / OSCA Staff",
    text: "Review, approve or reject Senior Citizen and PWD applications.",
    link: "Staff log in",
    target: "login",
  },
  {
    key: "establishment",
    icon: Store,
    title: "Establishments",
    text: "Check that an ID is valid and record the discounts you give.",
    link: "Establishment log in",
    target: "login",
  },
  {
    key: "government",
    icon: ShieldCheck,
    title: "Government Agencies",
    text: "Oversee the registry and review tax relief claims.",
    link: "Agency log in",
    target: "login",
  },
];

function HomePage({ openLogin, setPage, goSection }) {
  return (
    <main>
      {/* Hero */}
      <section className="hero">
        <div className="hero-copy">
          <div className="eyebrow">
            <span className="eyebrow-dot" /> A MORE CONNECTED WAY TO VERIFY
          </div>

          <h1>
            One platform.
            <br />
            <span>Two communities.</span>
            <br />
            Easier verification.
          </h1>

          <p className="hero-description">
            A unified digital platform designed to simplify Senior Citizen
            and PWD registration, ID verification, and benefits validation
            through a more secure and accessible process.
          </p>

          <div className="hero-actions">
            <button className="button primary" onClick={() => setPage("register")}>
              Register now <ArrowRight size={17} />
            </button>
            <button className="button secondary" onClick={() => openLogin()}>
              Log in
            </button>
          </div>

          <div className="hero-trust">
            <ShieldCheck size={17} />
            <span>Designed around verification, privacy, and accountability</span>
          </div>
        </div>

        <div className="hero-visual">
          <div className="visual-glow" />

          <div className="id-card">
            <div className="id-card-top">
              <div className="mini-seal">SC</div>
              <div>
                <b>REPUBLIC OF THE PHILIPPINES</b>
                <small>SENIOR CITIZEN IDENTIFICATION / PWD IDENTIFICATION</small>
              </div>
              <BadgeCheck className="verified-icon" size={22} />
            </div>

            <div className="id-card-content">
              <div className="avatar-placeholder">
                <Users size={33} />
              </div>
              <div className="id-details">
                <small>SAMPLE CARDHOLDER</small>
                <b>Juan Dela Cruz</b>
                <span>Senior Citizen ID</span>
                <strong>SC-0000-0001</strong>
              </div>
            </div>

            <div className="id-card-bottom">
              <span>SAMPLE · THIS IS NOT A VALID ID</span>
              <div className="fake-qr">
                <QrCode size={43} />
              </div>
            </div>
          </div>

          <div className="floating-check">
            <CheckCircle2 size={18} />
            <span>
              <b>Verification ready</b>
              <small>QR-based ID lookup</small>
            </span>
          </div>

          <div className="floating-lock">
            <LockKeyhole size={18} />
            <span>Privacy-conscious design</span>
          </div>
        </div>
      </section>

      {/* Stats strip */}
      <section className="stats-strip">
        <div>
          <strong>SC + PWD</strong>
          <span>one registry for both ID types</span>
        </div>
        <div>
          <strong>1 ID</strong>
          <span>same format nationwide</span>
        </div>
        <div>
          <strong>QR</strong>
          <span>quick checks at the counter</span>
        </div>
        <div>
          <strong>Private</strong>
          <span>shops only see what they need</span>
        </div>
      </section>

      {/* How it works */}
      <section className="section" id="how-it-works">
        <div className="section-heading">
          <span className="eyebrow">HOW IT WORKS</span>
          <h2>A simpler process for everyone.</h2>
          <p>
            CivicCareID PH connects individuals and organizations involved
            in Senior Citizen and PWD services through one platform.
          </p>
        </div>

        <div className="steps-grid">
          <Step
            number="01"
            icon={ClipboardList}
            title="Register once"
            text="Submit your Senior Citizen or PWD application with the required information and supporting documents."
          />
          <Step
            number="02"
            icon={FileCheck2}
            title="Get verified"
            text="Authorized personnel review the application and supporting documents before approval."
          />
          <Step
            number="03"
            icon={WalletCards}
            title="Receive your ID"
            text="Approved applicants receive their digital SC or PWD ID with a unique QR code."
          />
          <Step
            number="04"
            icon={QrCode}
            title="Verify and record"
            text="Participating establishments verify IDs and record eligible benefits."
          />
        </div>
      </section>

      {/* Portals */}
      <section className="portal-section" id="about">
        <div className="section-heading left">
          <span className="eyebrow">ONE PLATFORM, DIFFERENT PORTALS</span>
          <h2>Designed for every participant.</h2>
          <p>Everyone sees only the tools that belong to their role.</p>
        </div>

        <div className="portal-grid">
          {portals.map((item) => (
            <button
              className="portal-card"
              key={item.key}
              onClick={() => setPage(item.target)}
            >
              <span className="portal-icon">
                <item.icon size={22} />
              </span>
              <b>{item.title}</b>
              <p>{item.text}</p>
              <span className="portal-link">
                {item.link} <ArrowRight size={15} />
              </span>
            </button>
          ))}
        </div>
      </section>

      {/* Call to action */}
      <section className="cta">
        <div>
          <span className="eyebrow light">GET STARTED</span>
          <h2>
            Register once.
            <br />
            Use your ID anywhere.
          </h2>
          <p>Create an account to apply, or log in to continue.</p>
        </div>
        <div className="cta-actions">
          <button className="button white" onClick={() => setPage("register")}>
            Register now <ArrowRight size={17} />
          </button>
          <button className="button outline-light" onClick={() => openLogin()}>
            Log in
          </button>
        </div>
      </section>
    </main>
  );
}

function Step({ number, icon: Icon, title, text }) {
  return (
    <div className="step-card">
      <span className="step-number">{number}</span>
      <div className="step-icon">
        <Icon size={23} />
      </div>
      <h3>{title}</h3>
      <p>{text}</p>
    </div>
  );
}

/* ==========================================================================
   Login page
   ========================================================================== */

function LoginPage({ handleLogin, setPage }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [formError, setFormError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit() {
    setFormError("");
    setLoading(true);
    try {
      await handleLogin(email, password);
    } catch (err) {
      setFormError(err.message);
      setLoading(false);
    }
  }
  return (
    <main className="auth-layout">
      <div className="auth-aside">
        <div className="auth-aside-content">
          <span className="eyebrow light">WELCOME TO CIVICCAREID</span>
          <h1>
            One platform.
            <br />
            A more connected
            <br />
            community.
          </h1>
          <p>
            Log in to manage your registration, review applications or verify
            IDs. You will see the tools that match your account.
          </p>
          <div className="auth-points">
            <span>
              <ShieldCheck size={17} /> Role-based access
            </span>
            <span>
              <LockKeyhole size={17} /> Privacy-conscious records
            </span>
            <span>
              <HeartHandshake size={17} /> Built for accessibility
            </span>
          </div>
        </div>
      </div>

      <div className="auth-main">
        <form className="auth-form" onSubmit={(e) => { e.preventDefault(); submit(); }}>
          <button type="button" className="back-link" onClick={() => setPage("home")}>
            ← Back to home
          </button>

          <span className="eyebrow">ACCOUNT ACCESS</span>
          <h2>Log in to your portal</h2>
          <p className="muted">Enter your email and password to continue.</p>

          <label className="field-label" htmlFor="email">
            Email address
          </label>
          <input
            id="email"
            type="email"
            required
            autoComplete="email"
            placeholder="name@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />

          <label className="field-label" htmlFor="password">
            Password
          </label>
          <input
            id="password"
            type="password"
            required
            autoComplete="current-password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          {formError && (
            <p className="form-error" role="alert">{formError}</p>
          )}

          <button
            className="button primary full"
            type="submit"
            disabled={loading}
          >
            {loading ? "Logging in… (the server may be waking up)" : "Log in"}{" "}
            <ArrowRight size={17} />
          </button>


          <div className="auth-bottom">
            Don't have an account?{" "}
            <button type="button" onClick={() => setPage("register")}>
              Sign up
            </button>
          </div>
        </form>
      </div>
    </main>
  );
}

/* ==========================================================================
   Register page
   ========================================================================== */


const CLASSIFICATIONS = [
  ["DEAF_OR_HARD_OF_HEARING", "Deaf or Hard of Hearing"],
  ["INTELLECTUAL", "Intellectual Disability"],
  ["LEARNING", "Learning Disability"],
  ["MENTAL", "Mental Disability"],
  ["PHYSICAL", "Physical Disability"],
  ["PSYCHOSOCIAL", "Psychosocial Disability"],
  ["SPEECH_AND_LANGUAGE", "Speech and Language Impairment"],
  ["VISUAL", "Visual Disability"],
  ["CANCER", "Cancer"],
  ["RARE_DISEASE", "Rare Disease"],
];

// Only the demo LGU exists for now. More offices can be added here later.
const LGU_OPTIONS = [["QC-OSCA", "Quezon City OSCA (demo)"]];

const EMPTY_FORM = {
  fullName: "",
  email: "",
  password: "",
  birthDate: "",
  address: "",
  contactNumber: "",
  govIdNumber: "",
  lguCode: LGU_OPTIONS[0][0],
  classification: "",
};

function RegisterPage({ setPage, onRegistered }) {
  const [registrationType, setRegistrationType] = useState("senior");
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);
  // Once the account exists, a retry (e.g. after a validation error) skips sign-up.
  const [accountReady, setAccountReady] = useState(
    () => getSession()?.role === "CITIZEN"
  );
  const loggedInCitizen = getSession()?.role === "CITIZEN";
  const [done, setDone] = useState(false);

  function update(name) {
    return (e) => setForm({ ...form, [name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setFormError("");
    setFieldErrors({});
    setLoading(true);
    try {
      if (!accountReady) {
        await api("/auth/signup", {
          method: "POST", auth: false,
          body: { email: form.email, password: form.password, fullName: form.fullName },
        });
        setAccountReady(true);
        const session = await api("/auth/login", {
          method: "POST", auth: false,
          body: { email: form.email, password: form.password },
        });
        saveSession(session);
      }

      const isPwd = registrationType === "pwd";
      await api("/registrations", {
        method: "POST",
        body: {
          recordType: isPwd ? "PWD" : "SC",
          disabilityClassification: isPwd ? form.classification : null,
          fullName: form.fullName,
          birthDate: form.birthDate,
          address: form.address,
          contactNumber: form.contactNumber || null,
          govIdNumber: form.govIdNumber,
          lguCode: form.lguCode,
        },
      });
      setDone(true);
    } catch (err) {
      setFormError(err.message);
      setFieldErrors(err.fieldErrors ?? {});
    } finally {
      setLoading(false);
    }
  }

  function fieldError(name) {
    return fieldErrors[name] ? <small className="field-error">{fieldErrors[name]}</small> : null;
  }

  return (
    <main className="register-wrap">
      <button className="back-link" onClick={() => setPage("home")}>
        ← Back to home
      </button>

      <div className="register-card">
        <span className="eyebrow">SC / PWD REGISTRATION</span>
        <h2>{loggedInCitizen ? "Apply for an ID" : "Create a registration request"}</h2>
        <p className="muted">
          {loggedInCitizen
            ? "You are logged in. Choose the ID you want to apply for. Use the same government ID number as your other application."
            : "Select your registration type and enter your information below."}
        </p>

        {done ? (
          <div className="success-panel">
            <CheckCircle2 size={36} />
            <h3>Application submitted</h3>
            <p>
              Your account was created and your application is waiting for review
              by your local office. You are now logged in.
            </p>
            <button
              className="button primary"
              onClick={() => onRegistered()}
            >
              Go to my dashboard
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <label className="field-label">Registration type</label>

            <div className="role-options">
              <button
                type="button"
                className={registrationType === "senior" ? "role-option selected" : "role-option"}
                onClick={() => setRegistrationType("senior")}
              >
                <Users size={19} />
                <span>Senior Citizen</span>
                {registrationType === "senior" && <CheckCircle2 size={17} />}
              </button>

              <button
                type="button"
                className={registrationType === "pwd" ? "role-option selected" : "role-option"}
                onClick={() => setRegistrationType("pwd")}
              >
                <HeartHandshake size={19} />
                <span>Person with Disability</span>
                {registrationType === "pwd" && <CheckCircle2 size={17} />}
              </button>
            </div>

            <div className="form-grid">
              <div className="span-two">
                <label className="field-label">Full name</label>
                <input required value={form.fullName} onChange={update("fullName")}
                  placeholder="e.g. Juan Santos Dela Cruz" />
                {fieldError("fullName")}
              </div>

              <div>
                <label className="field-label">Date of birth</label>
                <input required type="date" value={form.birthDate} onChange={update("birthDate")} />
                {fieldError("birthDate")}
              </div>

              <div>
                <label className="field-label">Government ID number</label>
                <input required value={form.govIdNumber} onChange={update("govIdNumber")}
                  placeholder="e.g. PhilSys or UMID number" />
                {fieldError("govIdNumber")}
              </div>

              <div className="span-two">
                <label className="field-label">Home address</label>
                <input required value={form.address} onChange={update("address")}
                  placeholder="House no., street, barangay, city" />
                {fieldError("address")}
              </div>

              <div>
                <label className="field-label">Contact number (optional)</label>
                <input value={form.contactNumber} onChange={update("contactNumber")}
                  placeholder="09XX XXX XXXX" />
                {fieldError("contactNumber")}
              </div>

              <div>
                <label className="field-label">Local office (LGU)</label>
                <select required value={form.lguCode} onChange={update("lguCode")}>
                  {LGU_OPTIONS.map(([code, label]) => (
                    <option key={code} value={code}>{label}</option>
                  ))}
                </select>
                {fieldError("lguCode")}
              </div>

              {registrationType === "pwd" && (
                <div className="span-two">
                  <label className="field-label">Disability classification</label>
                  <select required value={form.classification} onChange={update("classification")}>
                    <option value="" disabled>Select classification</option>
                    {CLASSIFICATIONS.map(([value, label]) => (
                      <option key={value} value={value}>{label}</option>
                    ))}
                  </select>
                  {fieldError("disabilityClassification")}
                </div>
              )}

              {!accountReady && (
                <>
                  <div>
                    <label className="field-label">Email address</label>
                    <input required type="email" value={form.email} onChange={update("email")}
                      placeholder="you@example.com" />
                    {fieldError("email")}
                  </div>

                  <div>
                    <label className="field-label">Password (8+ characters)</label>
                    <input required type="password" minLength={8} value={form.password}
                      onChange={update("password")} autoComplete="new-password" />
                    {fieldError("password")}
                  </div>
                </>
              )}
            </div>

            {formError && <p className="form-error" role="alert">{formError}</p>}

            <button className="button primary full" type="submit" disabled={loading}>
              {loading ? "Submitting..." : "Submit application"} <ArrowRight size={17} />
            </button>

            <p className="auth-bottom">
              Already have an account?{" "}
              <button type="button" onClick={() => setPage("login")}>Log in</button>
            </p>

            <p className="table-footnote">
              Demo environment: use made-up details only. Your government ID number is
              scrambled and never stored as typed.
            </p>
          </form>
        )}
      </div>
    </main>
  );
}

/* ==========================================================================
   Dashboard
   ========================================================================== */

function getTabsForRole(role) {
  if (role === "citizen") return ["Overview", "My Digital ID"];
  if (role === "lgu") return ["Overview", "Applications"];
  if (role === "establishment") return ["Overview", "Verify ID", "Discounts"];
  return ["Overview", "Registry", "Tax Claims"];
}

function getTabIcon(tab, index) {
  if (index === 0) return <Home size={17} />;
  if (tab.includes("ID") || tab.includes("Verify")) return <QrCode size={17} />;
  if (tab.includes("Application")) return <ClipboardList size={17} />;
  return <WalletCards size={17} />;
}

function Dashboard({ role, setPage, logout, notice, setNotice }) {
  const [tab, setTab] = useState("Overview");
  const [verified, setVerified] = useState(false);
  const [discounts, setDiscounts] = useState(12);

  const selected = roles.find((r) => r.id === role) || roles[0];
  const tabs = getTabsForRole(role);

  // Who is logged in (name shown in the sidebar and top bar)
  const [account, setAccount] = useState(null);
  useEffect(() => {
    api("/me").then(setAccount).catch(() => {});
  }, []);
  const displayName = account?.fullName ?? "…";

  function selectTab(name) {
    setTab(name);
    setVerified(false);
  }

  return (
    <main className="dashboard-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-mark logo-text">CC</span>
          <b>CivicCareID</b>
        </div>

        <div className="sidebar-label">WORKSPACE</div>
        {tabs.map((t, i) => (
          <button
            key={t}
            className={tab === t ? "side-link active" : "side-link"}
            onClick={() => selectTab(t)}
          >
            {getTabIcon(t, i)} {t}
          </button>
        ))}

        <div className="sidebar-bottom">
          <div className="profile-mini">
            <span>{displayName.charAt(0)}</span>
            <div>
              <b>{displayName}</b>
              <small>{selected.label}</small>
            </div>
          </div>
          <button className="side-link" onClick={logout}>
            <LogIn size={17} /> Log out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <section className="dashboard-main">
        <div className="dash-top">
          <div>
            <span className="eyebrow">CIVICCAREID PH / WORKSPACE</span>
            <h1>{tab}</h1>
            <p className="muted">
              Welcome back. Here's what's happening in your portal.
            </p>
          </div>
          <div className="dash-user">
            <span className="avatar-small">{displayName.charAt(0)}</span>
            <div>
              <b>{displayName}</b>
              <small>{selected.label}</small>
            </div>
            <ChevronDown size={16} />
          </div>
        </div>

        {notice && (
          <div className="demo-banner">
            <span>
              <BadgeCheck size={17} /> {notice}
            </span>
            <button onClick={() => setNotice("")}>×</button>
          </div>
        )}

        {role === "citizen" && (
          <CitizenContent tab={tab} setTab={selectTab} setPage={setPage} account={account} />
        )}
        {role === "lgu" && <LguContent tab={tab} setTab={selectTab} />}
        {role === "establishment" && (
          <EstablishmentContent
            tab={tab}
            verified={verified}
            setVerified={setVerified}
            discounts={discounts}
            setDiscounts={setDiscounts}
          />
        )}
        {role === "government" && <GovernmentContent tab={tab} />}
      </section>
    </main>
  );
}

function Metric({ label, value, note, icon: Icon }) {
  return (
    <div className="metric-card">
      <div className="metric-top">
        <span>{label}</span>
        <Icon size={19} />
      </div>
      <strong>{value}</strong>
      <small>{note}</small>
    </div>
  );
}

/* ==========================================================================
   Dashboard content: Citizen (Senior Citizen and/or PWD, real data)
   ========================================================================== */

const TYPE_LABEL = { SC: "Senior Citizen", PWD: "PWD" };

const STATUS_INFO = {
  SUBMITTED: { label: "Pending review", tone: "pending" },
  APPROVED: { label: "Approved", tone: "" },
  AMENDED: { label: "Approved (amended)", tone: "" },
  REJECTED: { label: "Not approved", tone: "rejected" },
  REVOKED: { label: "Revoked", tone: "rejected" },
  EXPIRED: { label: "Expired", tone: "rejected" },
};

function formatDate(value) {
  if (!value) return "";
  return new Date(value).toLocaleDateString("en-PH", {
    year: "numeric", month: "long", day: "numeric",
  });
}

function classificationLabel(value) {
  return CLASSIFICATIONS.find(([v]) => v === value)?.[1] ?? value;
}

function StatusPill({ status }) {
  const info = STATUS_INFO[status] ?? { label: status, tone: "pending" };
  return (
    <span className={`status-pill ${info.tone}`.trim()}>
      <CheckCircle2 size={15} /> {info.label}
    </span>
  );
}

function CitizenContent({ tab, setTab, setPage, account }) {
  const [items, setItems] = useState(null);
  const [error, setError] = useState("");

  function load() {
    setError("");
    api("/me/registrations")
      .then(setItems)
      .catch((err) => setError(err.message));
  }
  useEffect(load, []);

  if (error) {
    return (
      <div className="content-card">
        <p className="form-error" role="alert">{error}</p>
        <button className="button secondary" onClick={load}>Try again</button>
      </div>
    );
  }
  if (items === null) {
    return <div className="content-card"><p className="muted">Loading your records…</p></div>;
  }

  const firstName = account?.fullName?.split(" ")[0] ?? "";
  const missingTypes = ["SC", "PWD"].filter(
    (t) => !items.some((r) => r.recordType === t && ["SUBMITTED", "APPROVED", "AMENDED"].includes(r.status))
  );

  if (items.length === 0) {
    return (
      <div className="content-card">
        <span className="eyebrow">GET STARTED</span>
        <h2>Hello{firstName ? `, ${firstName}` : ""}.</h2>
        <p className="muted">
          You have not applied for an ID yet. Apply for a Senior Citizen or PWD
          ID and your local office will review it.
        </p>
        <button className="button primary" onClick={() => setPage("register")}>
          Apply for an ID <ArrowRight size={17} />
        </button>
      </div>
    );
  }

  if (tab === "My Digital ID") {
    const cards = items.filter((r) => r.idCard && (r.status === "APPROVED" || r.status === "AMENDED"));
    if (cards.length === 0) {
      return (
        <div className="content-card">
          <span className="eyebrow">DIGITAL IDENTIFICATION</span>
          <h2>Your ID is not ready yet</h2>
          <p className="muted">
            Your digital ID appears here as soon as your application is approved.
            You can follow its progress on the Overview page.
          </p>
          <button className="button secondary" onClick={() => setTab("Overview")}>
            Go to overview
          </button>
        </div>
      );
    }
    return (
      <div className="id-stack">
        {cards.map((r) => (
          <div className="content-card id-preview" key={r.id}>
            <div>
              <span className="eyebrow">DIGITAL IDENTIFICATION</span>
              <h2>{TYPE_LABEL[r.recordType]} ID</h2>
              <p className="muted">
                Issued {formatDate(r.idCard.issuedAt)} · Valid until {formatDate(r.idCard.validUntil)}
              </p>
            </div>

            <div className="sample-id">
              <div className="id-card-top">
                <div className="mini-seal">{r.recordType}</div>
                <div>
                  <b>REPUBLIC OF THE PHILIPPINES</b>
                  <small>
                    {r.recordType === "SC"
                      ? "SENIOR CITIZEN IDENTIFICATION"
                      : "PERSON WITH DISABILITY IDENTIFICATION"}
                  </small>
                </div>
                <BadgeCheck size={22} />
              </div>

              <div className="sample-id-person">
                <div className="avatar-placeholder">
                  {r.recordType === "SC" ? <Users size={30} /> : <HeartHandshake size={30} />}
                </div>
                <div>
                  <small>NAME</small>
                  <b>{r.applicant.fullName}</b>
                  <small>{r.recordType} ID NUMBER</small>
                  <b>{r.idCard.cardNumber}</b>
                </div>
                <div className="fake-qr">
                  <QrCode size={57} />
                </div>
              </div>

              <div className="id-warning">
                PROTOTYPE · SYNTHETIC DATA · NOT VALID FOR IDENTIFICATION
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  const approved = items.filter((r) => r.status === "APPROVED" || r.status === "AMENDED").length;
  const pending = items.filter((r) => r.status === "SUBMITTED").length;

  return (
    <>
      <div className="metric-grid">
        <Metric label="Applications" value={items.length} note="Senior Citizen and PWD" icon={ClipboardList} />
        <Metric label="Approved IDs" value={approved} note="Ready to use" icon={BadgeCheck} />
        <Metric label="Waiting for review" value={pending} note="With your local office" icon={FileCheck2} />
      </div>

      <div className="content-card">
        <span className="eyebrow">YOUR APPLICATIONS</span>
        <h2>Hello{firstName ? `, ${firstName}` : ""}.</h2>

        {items.map((r) => (
          <div className="registration-card" key={r.id}>
            <div className="status-row">
              <b>{TYPE_LABEL[r.recordType]} ID</b>
              <StatusPill status={r.status} />
            </div>

            <div className="oversight-row">
              <span>Submitted</span>
              <b>{formatDate(r.submittedAt)}</b>
            </div>
            <div className="oversight-row">
              <span>Local office</span>
              <b>{r.lguCode}</b>
            </div>
            {r.recordType === "PWD" && r.disabilityClassification && (
              <div className="oversight-row">
                <span>Disability classification</span>
                <b>{classificationLabel(r.disabilityClassification)}</b>
              </div>
            )}
            {r.idCard && (
              <div className="oversight-row">
                <span>ID number</span>
                <b>{r.idCard.cardNumber}</b>
              </div>
            )}
            {r.reason && (
              <div className="oversight-row">
                <span>Reason from your local office</span>
                <b>{r.reason}</b>
              </div>
            )}
            {r.status === "SUBMITTED" && (
              <p className="muted">
                Your application is waiting for review. This page updates when you refresh it.
              </p>
            )}
          </div>
        ))}

        <div className="btn-row">
          <button className="button secondary" onClick={load}>Refresh</button>
          {missingTypes.length > 0 && (
            <button className="button primary" onClick={() => setPage("register")}>
              Apply for {missingTypes.map((t) => TYPE_LABEL[t]).join(" or ")} ID <ArrowRight size={17} />
            </button>
          )}
        </div>
      </div>
    </>
  );
}


/* ==========================================================================
   Dashboard content: LGU / OSCA staff (real review queue)
   ========================================================================== */

const LGU_FILTERS = [
  ["SUBMITTED", "Pending"],
  ["APPROVED", "Approved"],
  ["REJECTED", "Not approved"],
  ["REVOKED", "Revoked"],
  ["ALL", "All"],
];

function ageFrom(dateString) {
  const born = new Date(dateString);
  const now = new Date();
  let age = now.getFullYear() - born.getFullYear();
  const monthDiff = now.getMonth() - born.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < born.getDate())) age -= 1;
  return age;
}

function LguContent({ tab, setTab }) {
  const [counts, setCounts] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    Promise.all(
      ["SUBMITTED", "APPROVED", "REJECTED", "REVOKED"].map((status) =>
        api(`/registrations?status=${status}&size=1`).then((r) => r.totalElements)
      )
    )
      .then(([pending, approved, rejected, revoked]) =>
        setCounts({ pending, approved, notApproved: rejected + revoked })
      )
      .catch(() => setCounts(null));
  }, [refreshKey]);

  const show = (n) => (counts ? n : "…");
  const changed = () => setRefreshKey((k) => k + 1);

  return (
    <>
      <div className="metric-grid">
        <Metric label="Pending applications" value={show(counts?.pending)} note="Waiting for review" icon={ClipboardList} />
        <Metric label="Approved IDs" value={show(counts?.approved)} note="Currently valid" icon={CheckCircle2} />
        <Metric label="Not approved" value={show(counts?.notApproved)} note="Rejected or revoked" icon={ShieldCheck} />
      </div>

      {tab === "Applications" ? (
        <ApplicationsPanel refreshKey={refreshKey} onChanged={changed} />
      ) : (
        <ApplicationsPanel
          compact
          refreshKey={refreshKey}
          onChanged={changed}
          onSeeAll={() => setTab("Applications")}
        />
      )}
    </>
  );
}

function ApplicationsPanel({ compact = false, refreshKey, onChanged, onSeeAll }) {
  const [filter, setFilter] = useState("SUBMITTED");
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [openId, setOpenId] = useState(null);
  const [flash, setFlash] = useState("");
  const size = compact ? 5 : 10;

  function load() {
    setError("");
    const params = new URLSearchParams({ page, size });
    if (filter !== "ALL") params.set("status", filter);
    // Pending: oldest first (fair queue). Everything else: newest first.
    params.set("sort", filter === "SUBMITTED" ? "submittedAt,asc" : "submittedAt,desc");
    api(`/registrations?${params}`)
      .then(setData)
      .catch((err) => setError(err.message));
  }
  useEffect(load, [filter, page, refreshKey]);

  function pickFilter(value) {
    setFilter(value);
    setPage(0);
    setFlash("");
  }

  return (
    <div className="content-card">
      <div className="card-heading">
        <div>
          <span className="eyebrow">REGISTRATION QUEUE</span>
          <h2>{compact ? "Next in the queue" : "Applications"}</h2>
        </div>
        {compact && (
          <button className="button secondary small" onClick={onSeeAll}>
            See all applications
          </button>
        )}
      </div>

      {!compact && (
        <div className="filter-chips" role="group" aria-label="Filter by status">
          {LGU_FILTERS.map(([value, label]) => (
            <button
              key={value}
              className={filter === value ? "chip active" : "chip"}
              onClick={() => pickFilter(value)}
            >
              {label}
            </button>
          ))}
        </div>
      )}

      {flash && (
        <div className="demo-banner">
          <span><BadgeCheck size={17} /> {flash}</span>
          <button onClick={() => setFlash("")} aria-label="Dismiss">×</button>
        </div>
      )}

      {error && <p className="form-error" role="alert">{error}</p>}
      {!data && !error && <p className="muted">Loading applications…</p>}

      {data && data.content.length === 0 && (
        <p className="muted">
          {filter === "SUBMITTED"
            ? "All caught up. No applications are waiting for review."
            : "No applications match this filter."}
        </p>
      )}

      {data && data.content.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Applicant</th>
                <th>ID type</th>
                <th>Submitted</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((r) => (
                <tr key={r.id}>
                  <td><b>{r.fullName}</b></td>
                  <td>{TYPE_LABEL[r.recordType]}</td>
                  <td>{formatDate(r.submittedAt)}</td>
                  <td><StatusPill status={r.status} /></td>
                  <td>
                    <button className="table-action" onClick={() => setOpenId(r.id)}>
                      {r.status === "SUBMITTED" ? "Review" : "View"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!compact && data && data.totalPages > 1 && (
        <div className="pager">
          <button className="button secondary small" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </button>
          <span className="muted">Page {page + 1} of {data.totalPages}</span>
          <button
            className="button secondary small"
            disabled={page + 1 >= data.totalPages}
            onClick={() => setPage(page + 1)}
          >
            Next
          </button>
        </div>
      )}

      {openId && (
        <ReviewDialog
          id={openId}
          onClose={() => setOpenId(null)}
          onDone={(message) => {
            setOpenId(null);
            setFlash(message);
            load();
            onChanged();
          }}
        />
      )}
    </div>
  );
}

function ReviewDialog({ id, onClose, onDone }) {
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState("");
  const [mode, setMode] = useState(null); // "reject" | "revoke" | null
  const [reason, setReason] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api(`/registrations/${id}`).then(setDetail).catch((err) => setError(err.message));
  }, [id]);

  useEffect(() => {
    const onKey = (e) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  async function act(kind) {
    setBusy(true);
    setError("");
    try {
      await api(`/registrations/${id}/${kind}`, {
        method: "POST",
        body: kind === "approve" ? undefined : { reason: reason.trim() },
      });
      const verb = { approve: "Approved", reject: "Rejected", revoke: "Revoked" }[kind];
      onDone(`${verb} ${detail.applicant.fullName}'s ${TYPE_LABEL[detail.recordType]} application.`);
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  const canDecide = detail?.status === "SUBMITTED";
  const canRevoke = detail && (detail.status === "APPROVED" || detail.status === "AMENDED");

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="review-title"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="card-heading">
          <div>
            <span className="eyebrow">APPLICATION DETAILS</span>
            <h2 id="review-title">
              {detail ? `${TYPE_LABEL[detail.recordType]} ID` : "Loading…"}
            </h2>
          </div>
          <button className="icon-button" onClick={onClose} aria-label="Close">
            <X size={18} />
          </button>
        </div>

        {detail && (
          <div className="detail-list">
            <div className="oversight-row">
              <span>Status</span>
              <StatusPill status={detail.status} />
            </div>
            <div className="oversight-row">
              <span>Applicant</span>
              <b>{detail.applicant.fullName}</b>
            </div>
            <div className="oversight-row">
              <span>Date of birth</span>
              <b>{formatDate(detail.applicant.birthDate)} (age {ageFrom(detail.applicant.birthDate)})</b>
            </div>
            <div className="oversight-row">
              <span>Address</span>
              <b>{detail.applicant.address}</b>
            </div>
            {detail.applicant.contactNumber && (
              <div className="oversight-row">
                <span>Contact number</span>
                <b>{detail.applicant.contactNumber}</b>
              </div>
            )}
            {detail.recordType === "PWD" && detail.disabilityClassification && (
              <div className="oversight-row">
                <span>Disability classification</span>
                <b>{classificationLabel(detail.disabilityClassification)}</b>
              </div>
            )}
            <div className="oversight-row">
              <span>Local office</span>
              <b>{detail.lguCode}</b>
            </div>
            <div className="oversight-row">
              <span>Submitted</span>
              <b>{formatDate(detail.submittedAt)}</b>
            </div>
            {detail.reviewedAt && (
              <div className="oversight-row">
                <span>Reviewed</span>
                <b>{formatDate(detail.reviewedAt)}</b>
              </div>
            )}
            {detail.idCard && (
              <div className="oversight-row">
                <span>ID number</span>
                <b>{detail.idCard.cardNumber}</b>
              </div>
            )}
            {detail.reason && (
              <div className="oversight-row">
                <span>Reason</span>
                <b>{detail.reason}</b>
              </div>
            )}
          </div>
        )}

        {mode && (
          <div>
            <label className="field-label" htmlFor="reason">
              {mode === "reject" ? "Reason for rejecting" : "Reason for revoking"} (the applicant will see this)
            </label>
            <textarea
              id="reason"
              rows={3}
              maxLength={500}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              autoFocus
            />
          </div>
        )}

        {error && <p className="form-error" role="alert">{error}</p>}

        {detail && (canDecide || canRevoke) && (
          <div className="btn-row">
            {!mode && canDecide && (
              <>
                <button className="button primary" disabled={busy} onClick={() => act("approve")}>
                  {busy ? "Approving..." : "Approve"}
                </button>
                <button className="button danger" disabled={busy} onClick={() => setMode("reject")}>
                  Reject…
                </button>
              </>
            )}
            {!mode && canRevoke && (
              <button className="button danger" onClick={() => setMode("revoke")}>
                Revoke ID…
              </button>
            )}
            {mode && (
              <>
                <button
                  className="button danger solid"
                  disabled={busy || reason.trim() === ""}
                  onClick={() => act(mode)}
                >
                  {busy ? "Saving..." : mode === "reject" ? "Confirm rejection" : "Confirm revoke"}
                </button>
                <button
                  className="button secondary"
                  disabled={busy}
                  onClick={() => { setMode(null); setReason(""); setError(""); }}
                >
                  Cancel
                </button>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

/* ==========================================================================
   Dashboard content: Establishment
   ========================================================================== */

function EstablishmentContent({
  tab,
  verified,
  setVerified,
  discounts,
  setDiscounts,
}) {
  if (tab === "Verify ID") {
    return (
      <div className="content-card verify-card">
        <span className="eyebrow">QR ID VERIFICATION</span>
        <h2>Check an SC / PWD ID</h2>
        <p className="muted">
          Enter a sample ID reference to simulate a registry lookup.
        </p>

        <div className="verify-input">
          <input defaultValue="SC-0000-0001" aria-label="Sample ID reference" />
          <button className="button primary" onClick={() => setVerified(true)}>
            <QrCode size={17} /> Verify ID
          </button>
        </div>

        {verified && (
          <div className="verification-result">
            <CheckCircle2 size={23} />
            <div>
              <b>Sample ID verified</b>
              <p>Juan Dela Cruz · Registration status: Active</p>
              <small>
                Only limited eligibility information is displayed in this demo.
              </small>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <>
      <div className="metric-grid">
        <Metric
          label="IDs verified"
          value="86"
          note="This month · sample"
          icon={QrCode}
        />
        <Metric
          label="Discount records"
          value={discounts}
          note="Recorded in this demo"
          icon={WalletCards}
        />
        <Metric
          label="Claims status"
          value="Review"
          note="Sample claim status"
          icon={FileCheck2}
        />
      </div>

      <div className="content-card">
        <span className="eyebrow">QUICK ACTIONS</span>
        <h2>Manage senior discounts</h2>
        <p className="muted">
          Verify a customer's ID before recording an eligible discount.
        </p>

        <div className="action-row">
          <button className="button primary" onClick={() => setVerified(true)}>
            <QrCode size={17} /> Simulate ID verification
          </button>
          <button
            className="button secondary"
            onClick={() => setDiscounts(discounts + 1)}
          >
            <WalletCards size={17} /> Add sample discount record
          </button>
        </div>

        {verified && (
          <div className="verification-result">
            <CheckCircle2 size={22} />
            <div>
              <b>Sample ID verified</b>
              <p>Eligible registry status confirmed for demonstration.</p>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

/* ==========================================================================
   Dashboard content: Government agency
   ========================================================================== */

function GovernmentContent({ tab }) {
  const heading =
    tab === "Tax Claims"
      ? "Tax claim monitoring"
      : tab === "Registry"
      ? "Registry overview"
      : "System activity";

  return (
    <>
      <div className="metric-grid">
        <Metric
          label="Registered seniors"
          value="1,248"
          note="Illustrative total"
          icon={Users}
        />
        <Metric
          label="Participating establishments"
          value="86"
          note="Illustrative total"
          icon={Store}
        />
        <Metric
          label="Claims for review"
          value="17"
          note="Sample submissions"
          icon={FileCheck2}
        />
      </div>

      <div className="content-card">
        <span className="eyebrow">OVERSIGHT</span>
        <h2>{heading}</h2>
        

        <div className="oversight-row">
          <span>Registration records</span>
          <b>1,248</b>
        </div>
        <div className="oversight-row">
          <span>Discount transactions logged</span>
          <b>3,620</b>
        </div>
        <div className="oversight-row">
          <span>Claims awaiting review</span>
          <b>17</b>
        </div>
        <div className="oversight-row">
          <span>Duplicate flags for investigation</span>
          <b>3</b>
        </div>
      </div>
    </>
  );
}

export default App;
