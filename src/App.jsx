import React, { useState } from "react";
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

/* ==========================================================================
   Data
   ========================================================================== */

const roles = [
  {
    id: "senior",
    label: "SC Member",
    icon: Users,
    description: "Manage your Senior Citizen registration and digital ID.",
  },
  {
    id: "pwd",
    label: "PWD Member",
    icon: HeartHandshake,
    description: "Manage your PWD registration and digital ID.",
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

/* ==========================================================================
   App shell
   ========================================================================== */

function App() {
  const [page, setPage] = useState("home");
  const [role, setRole] = useState("senior");
  const [mobileMenu, setMobileMenu] = useState(false);
  const [notice, setNotice] = useState("");

  function openLogin(selectedRole = role) {
    setRole(selectedRole);
    setNotice("");
    setPage("login");
    setMobileMenu(false);
  }

  function enterDemo() {
    setPage("dashboard");
    setNotice(
      "Demo mode: the information shown is sample data, not a real government record."
    );
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <button
          className="brand"
          onClick={() => setPage("home")}
          aria-label="SeniorChain home"
        >
          <span className="brand-mark logo-text">SC</span>
          <span>
            <strong>SeniorChain</strong>
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
          <button onClick={() => setPage("home")}>Home</button>
          <a href="#how-it-works" onClick={() => setMobileMenu(false)}>
            How it works
          </a>
          <a href="#about" onClick={() => setMobileMenu(false)}>
            About
          </a>
          <button className="nav-login" onClick={() => openLogin()}>
            Log in <ArrowRight size={16} />
          </button>
        </nav>
      </header>

      {page === "home" && <HomePage openLogin={openLogin} />}
      {page === "login" && (
        <LoginPage
          role={role}
          setRole={setRole}
          enterDemo={enterDemo}
          setPage={setPage}
        />
      )}
      {page === "register" && <RegisterPage setPage={setPage} />}
      {page === "dashboard" && (
        <Dashboard
          role={role}
          setPage={setPage}
          notice={notice}
          setNotice={setNotice}
        />
      )}

      <footer className="footer">
        <div className="footer-brand">
          <span className="brand-mark small logo-text">SC</span>
          <strong>SeniorChain PH</strong>
        </div>
        
        <span>@ Gabe and the Gang</span>
      </footer>
    </div>
  );
}

/* ==========================================================================
   Home page
   ========================================================================== */

function HomePage({ openLogin }) {
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
            <button
              className="button primary"
              onClick={() => openLogin("senior")}
            >
              Access your account <ArrowRight size={17} />
            </button>
            <a className="button secondary" href="#how-it-works">
              Explore how it works
            </a>
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
                <b>Alysse Angelie Dela Cruz</b>
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
          <strong>One</strong>
          <span>unified registry concept</span>
        </div>
        <div>
          <strong>Four</strong>
          <span>user access portals</span>
        </div>
        <div>
          <strong>QR</strong>
          <span>based ID verification</span>
        </div>
        <div>
          <strong>Secure</strong>
          <span>by design approach</span>
        </div>
      </section>

      {/* How it works */}
      <section className="section" id="how-it-works">
        <div className="section-heading">
          <span className="eyebrow">HOW IT WORKS</span>
          <h2>A simpler process for everyone.</h2>
          <p>
            <p>
              SeniorChain PH connects individuals and organizations involved
              in Senior Citizen and PWD services through one platform.
            </p>
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
          <p>Each user sees the tools relevant to their role.</p>
        </div>

        <div className="portal-grid">
          {roles.map((item) => (
            <button
              className="portal-card"
              key={item.id}
              onClick={() => openLogin(item.id)}
            >
              <span className="portal-icon">
                <item.icon size={22} />
              </span>
              <b>{item.label}</b>
              <p>{item.description}</p>
              <span className="portal-link">
                Continue <ArrowRight size={15} />
              </span>
            </button>
          ))}
        </div>
      </section>

      {/* Call to action */}
      <section className="cta">
        <div>
          <span className="eyebrow light">SENIORCHAIN PH</span>
          <h2>
            Making verification clearer,
            <br />
            safer, and more accessible.
          </h2>
          <p>Explore the prototype using a sample account.</p>
        </div>
        <button className="button white" onClick={() => openLogin()}>
          Explore the demo <ArrowRight size={17} />
        </button>
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

function LoginPage({ role, setRole, enterDemo, setPage }) {
  return (
    <main className="auth-layout">
      <div className="auth-aside">
        <div className="auth-aside-content">
          <span className="eyebrow light">WELCOME TO SENIORCHAIN</span>
          <h1>
            One platform.
            <br />
            A more connected
            <br />
            community.
          </h1>
          <p>
            Access the services and tools designed for your role in the senior
            citizen verification process.
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
        <div className="auth-form">
          <button className="back-link" onClick={() => setPage("home")}>
            ← Back to home
          </button>

          <span className="eyebrow">ACCOUNT ACCESS</span>
          <h2>Log in to your portal</h2>
          <p className="muted">Choose your account type to continue.</p>

          <label className="field-label">I am logging in as</label>
          <div className="role-options">
            {roles.map((item) => (
              <button
                key={item.id}
                className={
                  role === item.id ? "role-option selected" : "role-option"
                }
                onClick={() => setRole(item.id)}
              >
                <item.icon size={19} />
                <span>{item.label}</span>
                {role === item.id && <CheckCircle2 size={17} />}
              </button>
            ))}
          </div>

          <label className="field-label" htmlFor="email">
            Email address
          </label>
          <input id="email" type="email" placeholder="name@example.com" />

          <label className="field-label" htmlFor="password">
            Password
          </label>
          <input
            id="password"
            type="password"
            placeholder="Enter your password"
          />

          <button className="button primary full" onClick={enterDemo}>
            Continue to demo dashboard <ArrowRight size={17} />
          </button>


          <div className="auth-bottom">
            Don't have an account?{" "}
            <button onClick={() => setPage("register")}>
              Start registration
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}

/* ==========================================================================
   Register page
   ========================================================================== */


function RegisterPage({ setPage }) {
  const [submitted, setSubmitted] = useState(false);
  const [registrationType, setRegistrationType] = useState("senior");

  function handleSubmit(e) {
    e.preventDefault();
    setSubmitted(true);
  }

  return (
    <main className="register-wrap">
      <button className="back-link" onClick={() => setPage("home")}>
        ← Back to home
      </button>

      <div className="register-card">
        <span className="eyebrow">SC / PWD REGISTRATION</span>
        <h2>Create a registration request</h2>
        <p className="muted">
          Select your registration type and enter your information below.
        </p>

        {submitted ? (
          <div className="success-panel">
            <CheckCircle2 size={36} />
            <h3>Sample request submitted</h3>
            <p>
              Your demo application is ready for review.
              No information has been sent to a server.
            </p>
            <button className="button primary" onClick={() => setPage("home")}>
              Return home
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <label className="field-label">Registration type</label>

            <div className="role-options">
              <button
                type="button"
                className={
                  registrationType === "senior"
                    ? "role-option selected"
                    : "role-option"
                }
                onClick={() => setRegistrationType("senior")}
              >
                <Users size={19} />
                <span>Senior Citizen</span>
                {registrationType === "senior" && <CheckCircle2 size={17} />}
              </button>

              <button
                type="button"
                className={
                  registrationType === "pwd"
                    ? "role-option selected"
                    : "role-option"
                }
                onClick={() => setRegistrationType("pwd")}
              >
                <HeartHandshake size={19} />
                <span>Person with Disability</span>
                {registrationType === "pwd" && <CheckCircle2 size={17} />}
              </button>
            </div>

            <div className="form-grid">
              <div>
                <label className="field-label">First name</label>
                <input required placeholder="e.g. Gabe" />
              </div>

              <div>
                <label className="field-label">Last name</label>
                <input required placeholder="e.g. De Vera" />
              </div>

              <div>
                <label className="field-label">Date of birth</label>
                <input required type="date" />
              </div>

              <div>
                <label className="field-label">City / Municipality</label>
                <input required placeholder="Muntinlupa" />
              </div>

              <div className="span-two">
                <label className="field-label">Email address</label>
                <input
                  required
                  type="email"
                  placeholder="user@gmail.com"
                />
              </div>

              {registrationType === "pwd" && (
                <div className="span-two">
                  <label className="field-label">
                    Disability classification
                  </label>
                  <select required defaultValue="">
                    <option value="" disabled>
                      Select classification
                    </option>
                    <option value="deaf-hard-of-hearing">
                      Deaf or Hard of Hearing
                    </option>
                    <option value="intellectual">
                      Intellectual Disability
                    </option>
                    <option value="learning">
                      Learning Disability
                    </option>
                    <option value="mental">
                      Mental Disability
                    </option>
                    <option value="physical">
                      Physical Disability
                    </option>
                    <option value="psychosocial">
                      Psychosocial Disability
                    </option>
                    <option value="speech">
                      Speech and Language Impairment
                    </option>
                    <option value="visual">
                      Visual Disability
                    </option>
                    <option value="cancer">
                      Cancer
                    </option>
                    <option value="rare-disease">
                      Rare Disease
                    </option>
                  </select>
                </div>
              )}
            </div>

            <button className="button primary full" type="submit">
              Submit sample registration <ArrowRight size={17} />
            </button>

            <p className="table-footnote">
              This is a demonstration form. No actual application is submitted.
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
  if (role === "senior") return ["Overview", "My Digital ID"];
  if (role === "pwd") return ["Overview", "My Digital ID"];
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

function Dashboard({ role, setPage, notice, setNotice }) {
  const [tab, setTab] = useState("Overview");
  const [verified, setVerified] = useState(false);
  const [discounts, setDiscounts] = useState(12);
  const [applications, setApplications] = useState(8);

  const selected = roles.find((r) => r.id === role) || roles[0];
  const tabs = getTabsForRole(role);

  function selectTab(name) {
    setTab(name);
    setVerified(false);
  }

  return (
    <main className="dashboard-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-mark logo-text">SC</span>
          <b>SeniorChain</b>
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
            <span>{selected.label.charAt(0)}</span>
            <div>
              <b>Demo User</b>
              <small>{selected.label}</small>
            </div>
          </div>
          <button className="side-link" onClick={() => setPage("home")}>
            <LogIn size={17} /> Log out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <section className="dashboard-main">
        <div className="dash-top">
          <div>
            <span className="eyebrow">SENIORCHAIN PH / WORKSPACE</span>
            <h1>{tab}</h1>
            <p className="muted">
              Welcome back. Here's what's happening in your portal.
            </p>
          </div>
          <div className="dash-user">
            <span className="avatar-small">{selected.label.charAt(0)}</span>
            <div>
              <b>Demo User</b>
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

        {role === "senior" && <SeniorContent tab={tab} />}
        {role === "pwd" && <PwdContent tab={tab} />}
        {role === "lgu" && (
          <LguContent
            tab={tab}
            applications={applications}
            setApplications={setApplications}
          />
        )}
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
   Dashboard content: Senior Citizen
   ========================================================================== */

function SeniorContent({ tab }) {
  if (tab === "My Digital ID") {
    return (
      <div className="content-card id-preview">
        <div>
          <span className="eyebrow">DIGITAL IDENTIFICATION</span>
          <h2>Your sample Digital ID</h2>
          <p className="muted">
            This is a fictional demonstration card, not a valid identification
            document.
          </p>
        </div>

        <div className="sample-id">
          <div className="id-card-top">
            <div className="mini-seal">SC</div>
            <div>
              <b>REPUBLIC OF THE PHILIPPINES</b>
              <small>SENIOR CITIZEN IDENTIFICATION</small>
            </div>
            <BadgeCheck size={22} />
          </div>

          <div className="sample-id-person">
            <div className="avatar-placeholder">
              <Users size={30} />
            </div>
            <div>
              <small>NAME</small>
              <b>Alysse Angelie Dela Cruz</b>
              <small>SC ID NUMBER</small>
              <b>SC-0000-0001</b>
            </div>
            <div className="fake-qr">
              <QrCode size={57} />
            </div>
          </div>

          <div className="id-warning">
            DEMO CARD · NOT VALID FOR IDENTIFICATION
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="metric-grid">
        <Metric
          label="Application status"
          value="Approved"
          note="Sample application"
          icon={BadgeCheck}
        />
        <Metric
          label="SC ID number"
          value="0001"
          note="Demo identifier"
          icon={WalletCards}
        />
        <Metric
          label="QR status"
          value="Active"
          note="Sample verification"
          icon={QrCode}
        />
      </div>

      <div className="content-card">
        <span className="eyebrow">YOUR REGISTRATION</span>
        <h2>You're all set, Alysse Angelie.</h2>
        <p className="muted">
          Your sample registration has been approved. You can view your digital
          ID and present its QR code to participating establishments for
          verification.
        </p>
        <div className="status-row">
          <span className="status-pill">
            <CheckCircle2 size={15} /> Approved
          </span>
          <span className="muted">Last updated: October 30, 2026</span>
        </div>
      </div>
    </>
  );
}


/* ==========================================================================
   Dashboard content: Person with Disability
   ========================================================================== */

function PwdContent({ tab }) {
  if (tab === "My Digital ID") {
    return (
      <div className="content-card id-preview">
        <div>
          <span className="eyebrow">DIGITAL IDENTIFICATION</span>
          <h2>Your sample PWD ID</h2>
          <p className="muted">
            This is a fictional demonstration card, not a valid identification
            document.
          </p>
        </div>

        <div className="sample-id">
          <div className="id-card-top">
            <div className="mini-seal">PWD</div>
            <div>
              <b>REPUBLIC OF THE PHILIPPINES</b>
              <small>PERSON WITH DISABILITY IDENTIFICATION</small>
            </div>
            <BadgeCheck size={22} />
          </div>

          <div className="sample-id-person">
            <div className="avatar-placeholder">
              <HeartHandshake size={30} />
            </div>
            <div>
              <small>NAME</small>
              <b>Juan Miguel Santos</b>
              <small>PWD ID NUMBER</small>
              <b>PWD-0000-0001</b>
              <small>DISABILITY CLASSIFICATION</small>
              <b>Physical Disability</b>
            </div>
            <div className="fake-qr">
              <QrCode size={57} />
            </div>
          </div>

          <div className="id-warning">
            DEMO CARD · NOT VALID FOR IDENTIFICATION
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="metric-grid">
        <Metric
          label="Application status"
          value="Approved"
          note="Sample application"
          icon={BadgeCheck}
        />
        <Metric
          label="PWD ID number"
          value="0001"
          note="Demo identifier"
          icon={WalletCards}
        />
        <Metric
          label="QR status"
          value="Active"
          note="Sample verification"
          icon={QrCode}
        />
      </div>

      <div className="content-card">
        <span className="eyebrow">YOUR REGISTRATION</span>
        <h2>Welcome back, Juan Miguel.</h2>
        <p className="muted">
          Your sample PWD registration has been approved. You can view
          your digital PWD ID and present its QR code to participating
          establishments for verification of your eligibility for applicable
          benefits and privileges.
        </p>

        <div className="status-row">
          <span className="status-pill">
            <CheckCircle2 size={15} /> Approved
          </span>
          <span className="muted">
            Last updated: October 30, 2026
          </span>
        </div>
      </div>

      <div className="content-card">
        <span className="eyebrow">PWD INFORMATION</span>
        <h2>Your registration details</h2>

        <div className="oversight-row">
          <span>Disability classification</span>
          <b>Physical Disability</b>
        </div>

        <div className="oversight-row">
          <span>Registration type</span>
          <b>PWD</b>
        </div>

        <div className="oversight-row">
          <span>Application reference</span>
          <b>APP-PWD-2026-001</b>
        </div>
      </div>
    </>
  );
}


/* ==========================================================================
   Dashboard content: LGU / OSCA staff
   ========================================================================== */

function LguContent({ tab, applications, setApplications }) {
  function approveOne() {
    setApplications(Math.max(0, applications - 1));
  }

  return (
    <>
      <div className="metric-grid">
        <Metric
          label="Pending applications"
          value={applications}
          note="Awaiting review"
          icon={ClipboardList}
        />
        <Metric
          label="Approved this month"
          value="124"
          note="Sample figure"
          icon={CheckCircle2}
        />
        <Metric
          label="Flagged duplicates"
          value="3"
          note="For manual review"
          icon={ShieldCheck}
        />
      </div>

      <div className="content-card">
        <div className="card-heading">
          <div>
            <span className="eyebrow">REGISTRATION QUEUE</span>
            <h2>
              {tab === "Applications"
                ? "Applications for review"
                : "Recent applications"}
            </h2>
          </div>
          <span className="muted">Sample records</span>
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Applicant</th>
                <th>Reference</th>
                <th>Date submitted</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Alysse Angelie Dela Cruz</td>
                <td>APP-2026-014</td>
                <td>Oct 29, 2026</td>
                <td>
                  <span className="status-pill pending">Pending</span>
                </td>
                <td>
                  <button className="table-action" onClick={approveOne}>
                    Approve demo
                  </button>
                </td>
              </tr>
              <tr>
                <td>Elena Reyes</td>
                <td>APP-2026-013</td>
                <td>Oct 28, 2026</td>
                <td>
                  <span className="status-pill">Approved</span>
                </td>
                <td>
                  <span className="muted">Reviewed</span>
                </td>
              </tr>
              <tr>
                <td>Ramon Garcia</td>
                <td>APP-2026-012</td>
                <td>Oct 27, 2026</td>
                <td>
                  <span className="status-pill pending">Pending</span>
                </td>
                <td>
                  <button className="table-action" onClick={approveOne}>
                    Approve demo
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <p className="table-footnote">
          Actions update this page's sample counters only. No actual application
          is approved.
        </p>
      </div>
    </>
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
              <p>Alysse Angelie Dela Cruz · Registration status: Active</p>
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
